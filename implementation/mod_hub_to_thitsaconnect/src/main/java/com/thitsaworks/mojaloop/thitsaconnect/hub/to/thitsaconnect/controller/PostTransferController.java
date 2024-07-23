package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.controller;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.component.mojaloop.ErrorCode;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Extension;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransferState;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisPayeeService;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayeeOutput;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.MojaloopApiErrorCodes;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.DecryptIlpPreparePacket;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.ValidateCondition;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.ValidateIlpPacket;
import com.thitsaworks.mojaloop.thitsaconnect.jws.JwsConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IGenerateSignature;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IValidateSignature;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.FspClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.FspParty;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.ReservationForTransfer;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubFromPayeeClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.exception.FspiopApiException;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerPreparePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;

@RestController
public class PostTransferController {

    @Autowired
    FspClient fspClient;

    @Autowired
    ThitsaconnectToHubFromPayeeClient thitsaconnectToHubFromPayeeClient;

    @Autowired
    DecryptIlpPreparePacket decryptIlpPreparePacket;

    @Autowired
    ValidateIlpPacket validateIlpPacket;

    @Autowired
    ValidateCondition validateCondition;

    @Autowired
    private IGenerateSignature generateSignature;
    @Autowired
    private IValidateSignature validateSignature;

    @Autowired
    private RedisPayeeService redisPayeeService;

    @Autowired
    private Executor taskExecutor;

    @Autowired
    JwsConfiguration.Settings jwsConfigurationSettings;
    private static final Logger LOG = LoggerFactory.getLogger(PostTransferController.class);

    @RequestMapping(value = "/transfers", method = RequestMethod.POST)
    public ResponseEntity<Void> execute(@RequestHeader("fspiop-source") String fspiopSource,
                                        @RequestHeader("fspiop-destination") String fspiopDestination,
                                        @RequestHeader("date") String fspiopDate,
                                        // @RequestHeader("fspiop-encryption") String fspiopEncryption,
                                        @RequestHeader(value = "fspiop-signature", required = false) String fspiopSignature,
                                        @RequestHeader("fspiop-uri") String fspiopUri,
                                        @RequestHeader("fspiop-http-method") String fspiopHttpMethod,
                                        @RequestHeader("accept") String accept,
                                        @RequestHeader("content-type") String contentType,
                                        @RequestBody TransfersPostRequest request)
            throws Exception {


        LOG.info("Post Transfer request info from Hub to Payee thitsaconnect:" + new Gson().toJson(request));

        Boolean isValidSignature = true;

        if (jwsConfigurationSettings.getIsEnableJws()) {

            IValidateSignature.Output validateSignature = this.validateSignature.execute(new IValidateSignature.Input(
                "RS256",
                fspiopUri,
                fspiopSource,
                fspiopDestination,
                fspiopDate,
                fspiopHttpMethod,
                fspiopSignature,
                new Gson().toJson(request)));

            isValidSignature = validateSignature.getIsValid();
        }
        if (isValidSignature == true) {
            this.taskExecutor.execute(() -> {

                try {

                    HubToPayeeOutput redisOutput = redisPayeeService.getDataWithTransferId(request.getTransferId());

                    //Validate InterledgerData and Condition
                    try {

                        if (redisOutput == null) {

                            LOG.error("Transfer Redis Data Not Found with TransferID: " + request.getTransferId());
                            throw new InternalServerErrorException();
                        }

                        InterledgerPreparePacket decryptedIlpPreparePacket = this.decryptIlpPreparePacket.execute(
                                new DecryptIlpPreparePacket.Input(request.getIlpPacket(), request.getCondition(),
                                        request.getExpiration())).getDecryptedIlpPreparePacket();

                        boolean isIlpDataValid = this.validateIlpPacket.execute(
                                new ValidateIlpPacket.Input(request.getPayerFsp(), request.getPayeeFsp(),
                                        request.getAmount().getAmount(), request.getAmount().getCurrency(),
                                        decryptedIlpPreparePacket)).isValid();

                        if (!isIlpDataValid) {

                            ErrorInformationResponse errorInformationResponse = new ErrorInformationResponse().errorInformation(

                                    //According to SDK
                                    new ErrorInformation().errorCode(MojaloopApiErrorCodes.INTERNAL_SERVER_ERROR.getCode())
                                            .errorDescription(String.format(
                                                    "ILP Packet in transfer prepare for %s does not match quote",
                                                    request.getTransferId())).extensionList(
                                                    new ExtensionList().addExtensionItem(
                                                            new Extension().key(InterledgerErrorCode.F01_INVALID_PACKET.getCode())
                                                                    .value(InterledgerErrorCode.F01_INVALID_PACKET.getName()))));

                            LOG.info("Put Transfer Error Request from Payee thitsaconnect to Hub transferId:" +
                                    request.getTransferId());
                            LOG.info("Put Transfer Error Request from Payee thitsaconnect to Hub request:" +
                                    new Gson().toJson(errorInformationResponse));

                            this.thitsaconnectToHubFromPayeeClient.putTransfersError(request.getTransferId(), fspiopDestination,
                                    fspiopSource, fspiopDate, accept, contentType, errorInformationResponse);

                            redisPayeeService.removeFromMap(request.getTransferId());


                        }

                        //check whether condition and Ilp's condition are correct or not
                        ValidateCondition.Output validateCondition = this.validateCondition.execute(
                                new ValidateCondition.Input(decryptedIlpPreparePacket,
                                        redisOutput.getInterledgerData().getFulfillment(), request.getCondition(),
                                        request.getAmount().getCurrency()));

                        if (!validateCondition.isValid()) {

                            ErrorInformationResponse errorInformationResponse = new ErrorInformationResponse().errorInformation(

                                    //According to SDK
                                    new ErrorInformation().errorCode(MojaloopApiErrorCodes.INTERNAL_SERVER_ERROR.getCode())
                                            .errorDescription(String.format(
                                                    "ILP condition in transfer prepare for %s does not match quote",
                                                    request.getTransferId())).extensionList(
                                                    new ExtensionList().addExtensionItem(
                                                            new Extension().key(InterledgerErrorCode.F05_WRONG_CONDITION.getCode())
                                                                    .value(InterledgerErrorCode.F05_WRONG_CONDITION.getName()))));

                            LOG.info("Put Transfer Error Request from Payee thitsaconnect to Hub transferId:" +
                                    request.getTransferId());
                            LOG.info("Put Transfer Error Request from Payee thitsaconnect to Hub request:" +
                                    new Gson().toJson(errorInformationResponse));

                            this.thitsaconnectToHubFromPayeeClient.putTransfersError(request.getTransferId(), fspiopDestination,
                                    fspiopSource, fspiopDate, accept, contentType, errorInformationResponse);

                            redisPayeeService.removeFromMap(request.getTransferId());

                        }

                    } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | FspiopApiException |
                             ClassNotFoundException | InternalServerErrorException e) {

                        ErrorInformationResponse errorInformationResponse = new ErrorInformationResponse().errorInformation(

                                new ErrorInformation().errorCode(MojaloopApiErrorCodes.INTERNAL_SERVER_ERROR.getCode())
                                        .errorDescription(MojaloopApiErrorCodes.INTERNAL_SERVER_ERROR.getMessage()));

                        LOG.info(
                                "Put Transfer Error Request from Payee thitsaconnect to Hub transferId:" + request.getTransferId());
                        LOG.info("Put Transfer Error Request from Payee thitsaconnect to Hub request:" +
                                new Gson().toJson(errorInformationResponse));

                        this.thitsaconnectToHubFromPayeeClient.putTransfersError(request.getTransferId(), fspiopDestination,
                                fspiopSource, fspiopDate, accept, contentType, errorInformationResponse);

                        redisPayeeService.removeFromMap(request.getTransferId());


                    }

                    QuotesIDPutResponse postQuoteOutput = redisOutput.getQuotesIDPutResponse();

                    //Data to get from redis
                    ReservationForTransfer.Request.Quote quote =
                            new ReservationForTransfer.Request.Quote(request.getTransferId(), request.getTransferId(),
                                    new BigDecimal(postQuoteOutput.getTransferAmount().getAmount()),
                                    postQuoteOutput.getTransferAmount().getCurrency().toString(),
                                    new BigDecimal(postQuoteOutput.getPayeeReceiveAmount().getAmount()),
                                    postQuoteOutput.getPayeeReceiveAmount().getCurrency().toString(),
                                    new BigDecimal(postQuoteOutput.getPayeeFspFee().getAmount()),
                                    postQuoteOutput.getPayeeFspFee().getCurrency().toString(),
                                    new BigDecimal(postQuoteOutput.getPayeeFspCommission().getAmount()),
                                    postQuoteOutput.getPayeeFspCommission().getCurrency().toString(),
                                    postQuoteOutput.getExpiration(), postQuoteOutput.getGeoCode(),
                                    postQuoteOutput.getExtensionList());

                    FspParty to = new FspParty().assignQuoteParty(redisOutput.getPayeeParty());

                    FspParty from = new FspParty().assignQuoteParty(redisOutput.getPayerParty());

                    ExtensionList extensionList = null;

                    if (request.getExtensionList() != null) {

                        extensionList = new ExtensionList();

                        for (Extension extension : request.getExtensionList().getExtension()) {
                            extensionList.addExtensionItem(extension);
                        }

                    }

                    ReservationForTransfer.Request reserveTransferRequest =
                            new ReservationForTransfer.Request(request.getTransferId(), quote, from, to,
                                    redisOutput.getAmountType(), request.getAmount().getCurrency(),
                                    new BigDecimal(request.getAmount().getAmount()),
                                    redisOutput.getTransactionScenario().toString(), new ReservationForTransfer.Request.IlpPacket(),
                                    redisOutput.getNote(), extensionList);

                    Instant startTime = Instant.now();
                    ReservationForTransfer.Response reserveTransferResponse =
                            this.fspClient.doReservationForTransfer(reserveTransferRequest);
                    Instant endTime = Instant.now();

                    LOG.info("Payee DFSP response time :"+(endTime.toEpochMilli()-startTime.toEpochMilli()));

                    LOG.info("Transfer response from Payee:" + new Gson().toJson(reserveTransferResponse));


                    if (reserveTransferResponse.getError() != null) {

                        ErrorInformationResponse errorInformationResponse =
                                new ErrorInformationResponse().errorInformation(reserveTransferResponse.getError());

                        LOG.info(
                                "Put Transfer Error Request from Payee thitsaconnect to Hub transferId:" + request.getTransferId());
                        LOG.info("Put Transfer Error Request from Payee thitsaconnect to Hub request:" +
                                new Gson().toJson(errorInformationResponse));

                        this.thitsaconnectToHubFromPayeeClient.putTransfersError(request.getTransferId(), fspiopDestination,
                                fspiopSource, fspiopDate, accept, contentType, errorInformationResponse);

                        redisPayeeService.removeFromMap(request.getTransferId());

                    }

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    String completedTimestamp = formatter.format(Instant.now().atOffset(ZoneOffset.UTC));

                    TransfersIDPutResponse transfersIDPutResponse =
                            new TransfersIDPutResponse().transferState(TransferState.RESERVED)
                                    .fulfilment(redisOutput.getInterledgerData().getFulfillment())
                                    .completedTimestamp(completedTimestamp)
                                    .extensionList(request.getExtensionList());


                    //String signatureGenerateFspiopDate = java.time.Instant.now().atZone(java.time.ZoneOffset.UTC).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'"));
                    String signatureGenerateFspiopDate = java.time.Instant.now().atZone(java.time.ZoneOffset.UTC).format(java.time.format.DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss 'GMT'"));

                    String fspiopSignatureValue = fspiopSignature;

                    if (jwsConfigurationSettings.getIsEnableJws()) {
                        IGenerateSignature.Output
                            payeeFspiopSignature =
                            this.generateSignature.execute(new IGenerateSignature.Input
                                                               ("RS256", "/transfers" + "/" + request.getTransferId(),
                                                                fspiopDestination,
                                                                fspiopSource,
                                                                signatureGenerateFspiopDate,
                                                                new Gson().toJson(transfersIDPutResponse),
                                                                HttpMethod.PUT.toString()
                                                               ));
                        fspiopSignatureValue = payeeFspiopSignature.getSignatureObject();

                    }
                    //PutQuote Call to Switch
                    LOG.info("Put Transfer Request from Payee thitsaconnect to Hub transferId:" + request.getTransferId());
                    LOG.info("Put Transfer Request from Payee thitsaconnect to Hub request:" +
                            new Gson().toJson(transfersIDPutResponse));

                    this.thitsaconnectToHubFromPayeeClient.putTransfers(request.getTransferId(), fspiopDestination, fspiopSource,
                            signatureGenerateFspiopDate, "RS256", fspiopSignatureValue,
                            "/transfers" + "/" + request.getTransferId(), HttpMethod.PUT.toString(),
                            accept, contentType, transfersIDPutResponse);


                    redisOutput.setTransfersIDPutResponse(transfersIDPutResponse);
                    redisPayeeService.putToRedisMap(redisOutput);

                   // redisPayeeService.removeFromMap(request.getTransferId());


                } catch (Exception e) {

                    throw new RuntimeException(e);
                }
            });

        } else {
            ErrorInformation errorInformation = new ErrorInformation();
            errorInformation.setErrorCode(ErrorCode.INVALID_SIGNATURE.getStatusCode().toString());
            errorInformation.setErrorDescription(ErrorCode.INVALID_SIGNATURE.getDefaultMessage());

            ErrorInformationResponse errorInfoRequest = new ErrorInformationResponse();

            errorInfoRequest.errorInformation(errorInformation);

            LOG.info("Put Party Error Request from Payee thitsaconnect to Hub:" + new Gson().toJson(errorInfoRequest));

            this.thitsaconnectToHubFromPayeeClient.putTransfersError(request.getTransferId(), fspiopDestination,
                    fspiopSource, fspiopDate, accept, contentType, errorInfoRequest);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
