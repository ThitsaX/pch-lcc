package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.component.mojaloop.ErrorCode;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.GeoCode;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionType;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisPayeeService;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayeeOutput;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.InterledgerData;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.EncryptIlpPreparePacket;
import com.thitsaworks.mojaloop.thitsaconnect.jws.JwsConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IGenerateSignature;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IValidateSignature;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.FspClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.FspParty;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.Quote;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubFromPayeeClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.Executor;

@RestController
public class PostQuoteController {

    @Autowired
    FspClient fspClient;

    @Autowired
    EncryptIlpPreparePacket encryptIlpPreparePacket;

    @Autowired
    private IValidateSignature validateSignature;

    @Autowired
    private IGenerateSignature generateSignature;

    @Autowired
    private RedisPayeeService redisPayeeService;

    @Autowired
    private Executor taskExecutor;

    @Autowired
    ThitsaconnectToHubFromPayeeClient thitsaconnectToHubFromPayeeClient;

    @Autowired
    JwsConfiguration.Settings jwsConfigurationSettings;

    private static final Logger LOG = LoggerFactory.getLogger(PostQuoteController.class);

    @RequestMapping(
        value = "/quotes",
        method = RequestMethod.POST)
    public ResponseEntity<Void> execute(@RequestHeader("fspiop-source") String fspiopSource,
                                        @RequestHeader("fspiop-destination") String fspiopDestination,
                                        // @RequestHeader("fspiop-encryption") String fspiopEncryption,
                                        @RequestHeader(value = "fspiop-signature",required = false) String fspiopSignature,
                                        @RequestHeader("fspiop-uri") String fspiopUri,
                                        @RequestHeader("fspiop-http-method") String fspiopHttpMethod,
                                        @RequestHeader("date") String fspiopDate,
                                        @RequestHeader("accept") String accept,
                                        @RequestHeader("content-type") String contentType,
                                        @RequestBody QuotesPostRequest request)
        throws Exception {

        LOG.info("Post Quote request info from Hub to Payee thitsaconnect:" + new Gson().toJson(request));
        LOG.info("jws Signature :" + fspiopSignature);

        Boolean isValidSignature = true;

        if (jwsConfigurationSettings.getIsEnableJws()) {

            LOG.info("jws validation did:");

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

                    String fspiopFormattedDate = java.time.ZonedDateTime.parse(fspiopDate,
                                                                               java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME)
                                                                        .toInstant()
                                                                        .toString();

                    HubToPayeeOutput redisOutput = new HubToPayeeOutput();
                    redisOutput.setPayeeParty(request.getPayee());
                    redisOutput.setPayerParty(request.getPayer());

                    FspParty payerParty = new FspParty().assignQuoteParty(request.getPayer());

                    FspParty payeeParty = new FspParty().assignQuoteParty(request.getPayee());

                    //Quote Call to CC
                    Quote.Response quoteResponse = this.fspClient.doQuote(
                        new Quote.Request(request.getQuoteId(),
                                          request.getTransactionId(),
                                          payeeParty,
                                          payerParty,
                                          request.getAmountType(),
                                          new BigDecimal(request.getAmount().getAmount()),
                                          request.getAmount().getCurrency(),
                                          new BigDecimal(request.getAmount().getAmount()),
                                          request.getAmount().getCurrency(),
                                          request.getTransactionType().toString(),
                                          request.getTransactionType().getInitiator().toString(),
                                          request.getTransactionType().getInitiatorType().toString(),
                                          request.getGeoCode(),
                                          request.getNote(),
                                          request.getExpiration(),
                                          request.getExtensionList()));

                    LOG.info("Quote response from Payee:" + new Gson().toJson(quoteResponse));

                    if (quoteResponse.getError() != null) {

                        ErrorInformationResponse errorInformationResponse =
                            new ErrorInformationResponse().errorInformation(quoteResponse.getError());

                        LOG.info(
                            "PutQuote Error Request from Payee thitsaconnect to Hub quoteId:" + request.getQuoteId());
                        LOG.info("PutQuote Error Request from Payee thitsaconnect to Hub:" +
                                     new Gson().toJson(errorInformationResponse));

                        this.thitsaconnectToHubFromPayeeClient.putQuotesError(request.getQuoteId(),
                                                                              fspiopDestination,
                                                                              fspiopSource,
                                                                              fspiopDate,
                                                                              accept,
                                                                              contentType,
                                                                              errorInformationResponse);
                    }

                    Money transferAmount = new Money().amount(quoteResponse.getTransferAmount().toString()).currency(
                        request.getAmount().getCurrency());
                    Money payeeReceivedAmount =
                        new Money().amount(quoteResponse.getPayeeReceiveAmount().toString())
                                   .currency(request.getAmount().getCurrency());

                    Money payeeFspFee = new Money().amount(quoteResponse.getPayeeFspFeeAmount().toString()).currency(
                        request.getAmount().getCurrency());

                    Money payeeFspCommission =
                        new Money().amount(quoteResponse.getPayeeFspCommissionAmount().toString())
                                   .currency(request.getAmount().getCurrency());

                    EncryptIlpPreparePacket.Output encryptIlpPreparePacket;

                    InterledgerData interledgerData =
                        new InterledgerData(request.getTransactionId(),
                                            request.getQuoteId(),
                                            request.getPayee(),
                                            request.getPayer(),
                                            transferAmount,
                                            request.getTransactionType(),
                                            request.getNote());

                    encryptIlpPreparePacket =
                        this.encryptIlpPreparePacket.execute(new EncryptIlpPreparePacket.Input(interledgerData));

                    String fulfillment = encodeBase64Url(encryptIlpPreparePacket.getInterledgerFulfillment()
                                                                                .getPreimage());

                    redisOutput.setInterledgerData(new HubToPayeeOutput.InterledgerData(fulfillment));

                    Instant instant = Instant.now().plusSeconds(600);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    String expiration = formatter.format(instant.atOffset(ZoneOffset.UTC));
                    quoteResponse.setExpiration(expiration);

                    String condition =
                        encodeBase64Url(encryptIlpPreparePacket.getInterledgerFulfillment().getCondition().getHash());

                    //Build PutQuote Request
                    QuotesIDPutResponse quotesIDPutResponse =
                        new QuotesIDPutResponse().transferAmount(transferAmount)
                                                 .payeeReceiveAmount(payeeReceivedAmount)
                                                 .payeeFspFee(payeeFspFee)
                                                 .payeeFspCommission(payeeFspCommission)
                                                 .expiration(quoteResponse.getExpiration())
                                                 .geoCode(quoteResponse.getGeoCode())
                                                 .ilpPacket(encryptIlpPreparePacket.getEncryptedIlpPreparePacket())
                                                 .condition(condition)
                                                 .extensionList(quoteResponse.getExtensionList());

                    String
                        signatureGenerateFspiopDate =
                        java.time.Instant.now()
                                         .atZone(java.time.ZoneOffset.UTC)
                                         .format(java.time.format.DateTimeFormatter.ofPattern(
                                             "E, dd MMM yyyy HH:mm:ss 'GMT'"));

                    String fspiopSignatureValue = fspiopSignature;

                    if (jwsConfigurationSettings.getIsEnableJws()) {

                        IGenerateSignature.Output
                            payeeFspiopSignature =
                            this.generateSignature.execute(new IGenerateSignature.Input
                                                               ("RS256", "/quotes" + "/" + quoteResponse.getQuoteId(),
                                                                fspiopDestination,
                                                                fspiopSource,
                                                                signatureGenerateFspiopDate,
                                                                new Gson().toJson(quotesIDPutResponse),
                                                                HttpMethod.PUT.toString()
                                                               ));

                        fspiopSignatureValue = payeeFspiopSignature.getSignatureObject();

                    }

                    LOG.info("Put Quote Request from Payee thitsaconnect to Hub quoteId:" +
                                 quoteResponse.getQuoteId());
                    LOG.info(
                        "Put Quote Request from Payee thitsaconnect to Hub:" + new Gson().toJson(quotesIDPutResponse));

                    //PutQuote Call to Switch
                    this.thitsaconnectToHubFromPayeeClient.putQuote(request.getQuoteId(),
                                                                    fspiopDestination,
                                                                    fspiopSource,
                                                                    signatureGenerateFspiopDate,
                                                                    "RS256",
                                                                    fspiopSignatureValue,
                                                                    "/quotes" + "/" + quoteResponse.getQuoteId(),
                                                                    "PUT",
                                                                    accept,
                                                                    contentType,
                                                                    quotesIDPutResponse);

                    redisOutput.setQuoteId(request.getQuoteId());
                    redisOutput.setTransferId(request.getTransactionId());
                    redisOutput.setNote(request.getNote());
                    redisOutput.setQuotesIDPutResponse(quotesIDPutResponse);
                    redisOutput.setAmountType(request.getAmountType());
                    redisOutput.setTransactionScenario(request.getTransactionType().getScenario());

                    boolean isRemittance = request.getTransactionType() != null &&
                        request.getTransactionType().getSubScenario() != null &&
                        "remittence".equalsIgnoreCase(request.getTransactionType().getSubScenario());

                    redisOutput.setRemittence(isRemittance);

                    redisPayeeService.putToRedisMap(redisOutput);

                } catch (Exception e) {

                    throw new RuntimeException(e);
                }
            });

        }
        else {

            ErrorInformation errorInformation = new ErrorInformation();
            errorInformation.setErrorCode(ErrorCode.INVALID_SIGNATURE.getStatusCode().toString());
            errorInformation.setErrorDescription(ErrorCode.INVALID_SIGNATURE.getDefaultMessage());

            ErrorInformationResponse errorInfoRequest = new ErrorInformationResponse();

            errorInfoRequest.errorInformation(errorInformation);

            LOG.info("Put Party Error Request from Payee thitsaconnect to Hub:" + new Gson().toJson(errorInfoRequest));

            this.thitsaconnectToHubFromPayeeClient.putQuotesError(request.getQuoteId(), fspiopDestination, fspiopSource,
                                                                  fspiopDate, accept, contentType, errorInfoRequest);
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Request implements Serializable {

        @JsonProperty("quoteId")
        private String quoteId;

        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("transactionRequestId")
        private String transactionRequestId;

        @JsonProperty("payee")
        private Party payee;

        @JsonProperty("payer")
        private Party payer;

        @JsonProperty("amountType")
        private AmountType amountType;

        @JsonProperty("amount")
        private Money amount;

        @JsonProperty("fees")
        private Money fees;

        @JsonProperty("transactionType")
        private TransactionType transactionType;

        @JsonProperty("geoCode")
        private GeoCode geoCode;

        @JsonProperty("note")
        private String note;

        @JsonProperty("expiration")
        private String expiration;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Response implements Serializable {

        @JsonProperty("status")
        private String status;

    }

    private static String encodeBase64Url(byte[] input) {

        return Base64.getUrlEncoder().encodeToString(input).replace("=", "");

    }

}


