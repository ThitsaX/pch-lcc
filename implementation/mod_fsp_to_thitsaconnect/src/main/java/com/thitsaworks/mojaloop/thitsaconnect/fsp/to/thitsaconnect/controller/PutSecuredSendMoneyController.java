package com.thitsaworks.mojaloop.thitsaconnect.fsp.to.thitsaconnect.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.component.error.ErrorResponse;
import com.thitsaworks.mojaloop.thitsaconnect.component.mojaloop.ErrorCode;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.to.thitsaconnect.FspToThitsaconnectConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyComplexName;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisPayerService;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayerOutput;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.StateEnum;
import com.thitsaworks.mojaloop.thitsaconnect.jws.JwsConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IGenerateSignature;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PostQuotes;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PostTransfers;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.exception.FspiopApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@RestController
public class PutSecuredSendMoneyController {

    private static final Logger LOG = LoggerFactory.getLogger(PutSecuredSendMoneyController.class);

    @Autowired
    private ThitsaconnectToHubClient thitsaconnectToHubClient;

    @Autowired
    private RedisPayerService redisPayerService;

    @Autowired
    private IGenerateSignature generateSignature;

    @Autowired
    FspToThitsaconnectConfiguration.Settings settings;

    @Autowired
    JwsConfiguration.Settings jwsConfigurationSettings;

    @RequestMapping(
        value = "/secured/acceptParty/{transferId}",
        method = RequestMethod.PUT)
    public ResponseEntity<Object> acceptParty(@Valid @PathVariable String transferId,
                                              @Valid @RequestBody AcceptPartyRequest request)
        throws Exception {

        LOG.info("AcceptParty Payer to thitsaconnect:" + new Gson().toJson(request));

        if (!request.getAcceptParty()) {

            redisPayerService.removeFromMap(transferId);
            return new ResponseEntity<>(new RejectResponse("false", null), HttpStatus.ACCEPTED);

        } else {

            HubToPayerOutput redisOutput = redisPayerService.getDataWithTransferId(transferId);

            try {

                if (redisOutput == null) {

                    LOG.error("AcceptParty Redis Data Not Found with TransferID: " + transferId);

                    throw new RuntimeException("No cached data found for transferId: " + transferId);
                }

                if (redisOutput.getPartiesRequest() != null && redisOutput.getPartiesResponse() != null) {

                    HubToPayerOutput.PartiesRequest partiesRequest = redisOutput.getPartiesRequest();
                    HubToPayerOutput.PartiesResponse partiesResponse = redisOutput.getPartiesResponse();

                    QuotesPostRequest quotesPostRequest = new QuotesPostRequest();

                    Money money = new Money();
                    money.setAmount(new BigDecimal(request.getAmount()).stripTrailingZeros().toPlainString());
                    money.setCurrency(partiesRequest.getCurrency());

                    quotesPostRequest.setQuoteId(partiesResponse.getTransferId());
                    quotesPostRequest.setTransactionId(partiesResponse.getTransferId());
                    quotesPostRequest.setPayee(partiesResponse.getPartiesTypeIDPutResponse().getParty());
                    quotesPostRequest.setPayer(redisOutput.getPayerParty().getParty());
                    quotesPostRequest.setAmountType(partiesRequest.getAmountType());
                    quotesPostRequest.setAmount(money);
                    quotesPostRequest.setTransactionType(partiesRequest.getTransactionType());
                    quotesPostRequest.setExtensionList(
                        partiesResponse.getPartiesTypeIDPutResponse()
                                       .getParty()
                                       .getPartyIdInfo()
                                       .getExtensionList());
                    quotesPostRequest.setNote(partiesRequest.getNote());

                    PostQuotes.Response postQuoteResponse;

                    LOG.info("QuoteRequest from thitsaconnect to Hub quoteId:" + transferId);
                    LOG.info("QuoteRequest from thitsaconnect to Hub quotePostRequest:" +
                                 new Gson().toJson(quotesPostRequest));

                    String fspiopDate = Instant.now()
                                               .atZone(java.time.ZoneOffset.UTC)
                                               .format(java.time.format.DateTimeFormatter.ofPattern(
                                                   "E, dd MMM yyyy HH:mm:ss 'GMT'"));

                    String fspiopSignatureValue = "";

                    if (jwsConfigurationSettings.getIsEnableJws()) {

                        IGenerateSignature.Output fspiopSignature =
                            this.generateSignature.execute(new IGenerateSignature.Input("RS256",
                                                                                        "/quotes",
                                                                                        redisOutput.getPayerParty()
                                                                                                   .getParty()
                                                                                                   .getPartyIdInfo()
                                                                                                   .getFspId(),
                                                                                        partiesResponse.getPartiesTypeIDPutResponse()
                                                                                                       .getParty()
                                                                                                       .getPartyIdInfo()
                                                                                                       .getFspId(),
                                                                                        fspiopDate,
                                                                                        new Gson().toJson(
                                                                                            quotesPostRequest),
                                                                                        HttpMethod.POST.toString()));

                        LOG.info("Signature object:" + new Gson().toJson(fspiopSignature.getSignatureObject()));

                        fspiopSignatureValue = fspiopSignature.getSignatureObject();

                    }

                    postQuoteResponse = this.thitsaconnectToHubClient.postQuote(
                        redisOutput.getPayerParty().getParty().getPartyIdInfo().getFspId(),
                        partiesResponse.getPartiesTypeIDPutResponse().getParty().getPartyIdInfo().getFspId(),
                        "RS256",
                        fspiopDate,
                        fspiopSignatureValue,
                        "/quotes",
                        "POST",
                        quotesPostRequest);

                    if (postQuoteResponse != null &&
                        postQuoteResponse.getStatus()
                                         .equalsIgnoreCase(Integer.toString(HttpStatus.ACCEPTED.value()))) {

                        HubToPayerOutput hubToPayerOutput = redisPayerService.subscribe(transferId);

                        if (hubToPayerOutput == null) {

                            LOG.info("AcceptParty no redis output obtained with transferID: " + transferId);

                            redisPayerService.removeFromMap(transferId);

                            throw new TimeoutException(
                                "AcceptParty no redis output obtained with transferID: " + transferId);

                        } else if (hubToPayerOutput.getPostQuoteOutput() == null &&
                            hubToPayerOutput.getErrorOutput() == null) {

                            LOG.info("AcceptParty no valid redis  output obtained with transferID:" + transferId);

                            redisPayerService.removeFromMap(transferId);

                            throw new TimeoutException(
                                "AcceptParty no valid redis  output obtained with transferID:" + transferId);

                        } else if (hubToPayerOutput.getErrorOutput() != null) {

                            LOG.info("AcceptParty Error Response from Hub to payer thitsaconnect:" +
                                         new Gson().toJson(hubToPayerOutput.getErrorOutput()));

                            ErrorInformationResponse errorInformationResponse =
                                hubToPayerOutput.getErrorOutput().getErrorInformationResponse();

                            if (errorInformationResponse == null) {

                                LOG.error("AcceptParty error response from Hub to payer thitsaconnect is null.");

                                throw new RuntimeException(
                                    "AcceptParty error response from Hub to payer thitsaconnect is null.");
                            }

                            redisPayerService.removeFromMap(transferId);

                            throw new RuntimeException();

                        } else if (hubToPayerOutput.getPostQuoteOutput().getQuotesIDPutResponse() == null) {

                            LOG.info("AcceptParty Invalid Response from Hub to payer thitsaconnect with transferID" +
                                         transferId);

                            redisPayerService.removeFromMap(transferId);

                            throw new RuntimeException();

                        } else {

                            redisPayerService.putToMap(transferId, hubToPayerOutput);

                            HubToPayerOutput.PostQuoteOutput postQuoteOutput = hubToPayerOutput.getPostQuoteOutput();

                            LOG.info("AcceptParty response from Hub to payer thitsaconnect:" +
                                         new Gson().toJson(postQuoteOutput.getQuotesIDPutResponse()));

                            Response response = new Response();
                            response.setTransferId(partiesResponse.getTransferId());
                            response.setHomeTransactionId(hubToPayerOutput.getPartiesRequest().getHomeTransactionId());

                            Party payerParty = redisOutput.getPayerParty().getParty();
                            FspParty from = new FspParty();
                            from.setType(redisOutput.getPayerParty().getType());
                            from.setIdType(payerParty.getPartyIdInfo().getPartyIdType());
                            from.setIdValue(payerParty.getPartyIdInfo().getPartyIdentifier());
                            from.setIdSubValue(payerParty.getPartyIdInfo().getPartySubIdOrType());
                            from.setFspId(payerParty.getPartyIdInfo().getFspId());
                            response.setFrom(from);

                            PartyIdInfo responsePartyIdInfo =
                                partiesResponse.getPartiesTypeIDPutResponse().getParty().getPartyIdInfo();

                            FspParty to = new FspParty();

                            to.setType(redisOutput.getPayeeParty().getType());

                            if (responsePartyIdInfo != null) {
                                to.setIdType(responsePartyIdInfo.getPartyIdType());
                                to.setIdValue(responsePartyIdInfo.getPartyIdentifier());
                                to.setIdSubValue(responsePartyIdInfo.getPartySubIdOrType());
                                to.setFspId(responsePartyIdInfo.getFspId());
                            }

                            if (partiesResponse.getPartiesTypeIDPutResponse().getParty().getPersonalInfo() != null) {

                                PartyComplexName partyComplexName =
                                    partiesResponse.getPartiesTypeIDPutResponse().getParty().getPersonalInfo()
                                                   .getComplexName();

                                to.setFirstName(partyComplexName.getFirstName());
                                to.setMiddleName(partyComplexName.getMiddleName());
                                to.setLastName(partyComplexName.getLastName());
                            }
                            if (partiesResponse.getPartiesTypeIDPutResponse().getParty().getName()!= null) {
                                to.setDisplayName(partiesResponse.getPartiesTypeIDPutResponse().getParty().getName());
                            }
                            to.setExtensionList(partiesResponse.getPartiesTypeIDPutResponse()
                                                               .getParty()
                                                               .getPartyIdInfo()
                                                               .getExtensionList());

                            List<Currency> supportedCurrenciesList =
                                partiesResponse.getPartiesTypeIDPutResponse().getParty().getSupportedCurrencies();
                            if (supportedCurrenciesList != null) {
                                to.setSupportedCurrencies(supportedCurrenciesList);
                            }

                            response.setTo(to);

                            response.setAmountType(partiesRequest.getAmountType());
                            response.setCurrency(
                                postQuoteOutput.getQuotesIDPutResponse().getTransferAmount().getCurrency());
                            response.setAmount(postQuoteOutput.getQuotesIDPutResponse()
                                                              .getTransferAmount()
                                                              .getAmount());
                            response.setPayeeReceiveAmount(postQuoteOutput.getQuotesIDPutResponse()
                                                                          .getPayeeReceiveAmount());
                            response.setPayeeFspFee(postQuoteOutput.getQuotesIDPutResponse()
                                                                   .getPayeeFspFee());
                            response.setPayeeFspCommission(postQuoteOutput.getQuotesIDPutResponse()
                                                                          .getPayeeFspCommission());
                            response.setTransactionType(partiesRequest.getTransactionType());
                            response.setCurrentState(postQuoteOutput.getCurrentState());
                            response.setInitiatedTimestamp(postQuoteOutput.getInitiatedTimestamp());
                            response.setDirection(partiesResponse.getDirection());
                            response.setNote(partiesRequest.getNote());

                            LOG.info("AcceptParty response from Payer thitsaconnect to payer :" +
                                         new Gson().toJson(response));

                            return new ResponseEntity<>(response, HttpStatus.OK);

                        }

                    }

                }
            } catch (FspiopApiException e) {

                String errorCode =
                        ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYEE.getStatusCode().toString(); //Default error code

                ErrorResponse errorResponse = new ErrorResponse();

                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode;

                if (!Objects.isNull(e.getErrorResponse()) && !Objects.isNull(e.getErrorResponse()
                                                                              .getErrorInformation())) {

                    errorCode = e.getErrorResponse().getErrorInformation().getErrorCode();

                    errorResponse.setDetailedDescription(e.getErrorResponse()
                                                          .getErrorInformation()
                                                          .getErrorDescription());

                }

                String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(errorCode,
                                                                                         settings.getLocaleLanguage());

                try {
                    jsonNode = objectMapper.readTree(localizedMessage);
                } catch (JsonProcessingException ex) {
                    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                errorResponse.setStatusCode(jsonNode.get("errorInformation").get("statusCode").asText());
                errorResponse.setMessage(jsonNode.get("errorInformation").get("description").asText());
                errorResponse.setLocaleMessage(jsonNode.get("errorInformation").get("descriptionLocale").asText());


                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

            } catch (TimeoutException e) {

                ErrorResponse errorResponse = new ErrorResponse();

                String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(
                        ErrorCode.SERVER_TIMED_OUT.getStatusCode().toString(), settings.getLocaleLanguage());

                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode;

                try {
                    jsonNode = objectMapper.readTree(localizedMessage);
                } catch (JsonProcessingException ex) {
                    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                errorResponse.setStatusCode(jsonNode.get("errorInformation").get("statusCode").asText());
                errorResponse.setMessage(jsonNode.get("errorInformation").get("description").asText());
                errorResponse.setLocaleMessage(jsonNode.get("errorInformation").get("descriptionLocale").asText());
                errorResponse.setDetailedDescription(e.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.REQUEST_TIMEOUT);

            } catch (ConnectException e) {

                ErrorResponse errorResponse = new ErrorResponse();

                String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(
                        ErrorCode.COMMUNICATION_ERROR.getStatusCode().toString(), settings.getLocaleLanguage());

                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode;

                try {
                    jsonNode = objectMapper.readTree(localizedMessage);
                } catch (JsonProcessingException ex) {
                    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
                }

                errorResponse.setStatusCode(jsonNode.get("errorInformation").get("statusCode").asText());
                errorResponse.setMessage(jsonNode.get("errorInformation").get("description").asText());
                errorResponse.setLocaleMessage(jsonNode.get("errorInformation").get("descriptionLocale").asText());
                errorResponse.setDetailedDescription("FSPIOP Connection Refused.");

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

            } catch (Exception e) {

                ErrorResponse errorResponse = new ErrorResponse();

                String errorCode =
                        ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYEE.getStatusCode().toString(); //default error code

                String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(errorCode,
                                                                                         settings.getLocaleLanguage());


                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode;

                try {
                    jsonNode = objectMapper.readTree(localizedMessage);
                } catch (JsonProcessingException ex) {
                    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                errorResponse.setStatusCode(jsonNode.get("errorInformation").get("statusCode").asText());
                errorResponse.setMessage(jsonNode.get("errorInformation").get("description").asText());
                errorResponse.setLocaleMessage(jsonNode.get("errorInformation").get("descriptionLocale").asText());
                errorResponse.setDetailedDescription(e.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

            }

        }

        LOG.info("AcceptParty API call response might be null or not accept.");
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(
        value = "/secured/acceptQuote/{transferId}",
        method = RequestMethod.PUT)
    public ResponseEntity<Object> acceptQuote(@Valid @PathVariable String transferId,
                                              @Valid @RequestBody AcceptQuoteRequest request) {

        LOG.info("AcceptQuote Payer to thitsaconnect:" + new Gson().toJson(request));

        if (!request.getAcceptQuote()) {

            redisPayerService.removeFromMap(transferId);
            return new ResponseEntity<>(new RejectResponse("true", "false"), HttpStatus.ACCEPTED);

        } else {

            HubToPayerOutput redisOutput = redisPayerService.getDataWithTransferId(transferId);

            try {

                if (redisOutput == null) {

                    LOG.error("AcceptQuote Redis Data Not Found with TransferID: " + transferId);

                    throw new RuntimeException("No cached data found for transferId: " + transferId);

                }

                if (redisOutput.getPostQuoteOutput() != null) {

                    QuotesIDPutResponse quoteCallOutput = redisOutput.getPostQuoteOutput().getQuotesIDPutResponse();

                    HubToPayerOutput.PartiesResponse partiesResponse = redisOutput.getPartiesResponse();
                    HubToPayerOutput.PartiesRequest partiesRequest = redisOutput.getPartiesRequest();
                    Party payerPartyData = redisOutput.getPayerParty().getParty();

                    TransfersPostRequest transfersPostRequest = new TransfersPostRequest();

                    transfersPostRequest.setTransferId(partiesResponse.getTransferId());
                    transfersPostRequest.setPayerFsp(payerPartyData.getPartyIdInfo().getFspId());
                    transfersPostRequest.setPayeeFsp(
                        partiesResponse.getPartiesTypeIDPutResponse().getParty().getPartyIdInfo().getFspId());
                    transfersPostRequest.setAmount(quoteCallOutput.getTransferAmount());
                    transfersPostRequest.setIlpPacket(quoteCallOutput.getIlpPacket());
                    transfersPostRequest.setCondition(quoteCallOutput.getCondition());
                    transfersPostRequest.setExpiration(quoteCallOutput.getExpiration());
                    transfersPostRequest.setExtensionList(quoteCallOutput.getExtensionList());

                    PostTransfers.Response postTransfersResponse;

                    LOG.info("Transfer Request from thitsaconnect to Hub quoteId:" + transferId);
                    LOG.info("Transfer Request from thitsaconnect to Hub transfersPostRequest:" +
                                 new Gson().toJson(transfersPostRequest));

                    String fspiopDate = Instant.now()
                                               .atZone(java.time.ZoneOffset.UTC)
                                               .format(java.time.format.DateTimeFormatter.ofPattern(
                                                   "E, dd MMM yyyy HH:mm:ss 'GMT'"));

                    String fspiopSignatureValue = "";

                    if (jwsConfigurationSettings.getIsEnableJws()) {

                        IGenerateSignature.Output
                            fspiopSignature =
                            this.generateSignature.execute(new IGenerateSignature.Input("RS256",
                                                                                        "/transfers",
                                                                                        redisOutput.getPayerParty()
                                                                                                   .getParty()
                                                                                                   .getPartyIdInfo()
                                                                                                   .getFspId(),
                                                                                        partiesResponse.getPartiesTypeIDPutResponse()
                                                                                                       .getParty()
                                                                                                       .getPartyIdInfo()
                                                                                                       .getFspId(),
                                                                                        fspiopDate,
                                                                                        new Gson().toJson(
                                                                                            transfersPostRequest),
                                                                                        HttpMethod.POST.toString()));

                        fspiopSignatureValue = fspiopSignature.getSignatureObject();
                    }

                    postTransfersResponse =
                        this.thitsaconnectToHubClient.postTransfers(payerPartyData.getPartyIdInfo().getFspId(),
                                                                    partiesResponse.getPartiesTypeIDPutResponse()
                                                                                   .getParty()
                                                                                   .getPartyIdInfo()
                                                                                   .getFspId(),
                                                                    "RS256",
                                                                    fspiopDate, fspiopSignatureValue,
                                                                    "/transfers",
                                                                    HttpMethod.POST.toString(),
                                                                    transfersPostRequest);

                    if (postTransfersResponse != null && postTransfersResponse.getStatus().equalsIgnoreCase(
                        Integer.toString(HttpStatus.ACCEPTED.value()))) {

                        Instant startTime = Instant.now();

                        HubToPayerOutput hubToPayerOutput = redisPayerService.subscribe(transferId);

                        Instant endTime = Instant.now();
                        LOG.info("Hub response time :" + (endTime.toEpochMilli() - startTime.toEpochMilli()));

                        if (hubToPayerOutput == null) {

                            LOG.info("AcceptQuote no redis output obtained with transferID: " + transferId);

                            redisPayerService.removeFromMap(transferId);

                            throw new TimeoutException(
                                "AcceptQuote no redis output obtained with transferID: " + transferId);

                        } else if (hubToPayerOutput.getPostTransfersOutput() == null &&
                            hubToPayerOutput.getErrorOutput() == null) {

                            LOG.info("AcceptQuote no valid redis output obtained with transferID:" + transferId);

                            redisPayerService.removeFromMap(transferId);

                            throw new TimeoutException(
                                "AcceptQuote no valid redis output obtained with transferID:" + transferId);

                        } else if (hubToPayerOutput.getErrorOutput() != null) {

                            LOG.info("AcceptQuote error response from Hub to payer thitsaconnect:" +
                                         new Gson().toJson(hubToPayerOutput.getErrorOutput()));

                            ErrorInformationResponse errorInformationResponse =
                                hubToPayerOutput.getErrorOutput().getErrorInformationResponse();

                            if (errorInformationResponse == null) {

                                LOG.error("AcceptQuote error response from Hub to payer thitsaconnect is null.");

                                throw new RuntimeException();

                            }

                            throw new FspiopApiException(errorInformationResponse);

                        } else {

                            HubToPayerOutput.PostTransfersOutput postTransfersOutput =
                                hubToPayerOutput.getPostTransfersOutput();

                            FspParty from = new FspParty();
                            from.setType(redisOutput.getPayerParty().getType());
                            from.setIdType(payerPartyData.getPartyIdInfo().getPartyIdType());
                            from.setIdValue(payerPartyData.getPartyIdInfo().getPartyIdentifier());
                            from.setIdSubValue(payerPartyData.getPartyIdInfo().getPartySubIdOrType());
                            from.setFspId(payerPartyData.getPartyIdInfo().getFspId());

                            Response response = new Response();
                            response.setFrom(from);

                            response.setTransferId(partiesResponse.getTransferId());
                            response.setHomeTransactionId(hubToPayerOutput.getPartiesRequest().getHomeTransactionId());

                            FspParty to = new FspParty();
                            PartyIdInfo responsePartyIdInfo =
                                partiesResponse.getPartiesTypeIDPutResponse().getParty().getPartyIdInfo();

                            to.setType(redisOutput.getPayeeParty().getType());

                            if (responsePartyIdInfo != null) {
                                to.setIdType(responsePartyIdInfo.getPartyIdType());
                                to.setIdValue(responsePartyIdInfo.getPartyIdentifier());
                                to.setIdSubValue(responsePartyIdInfo.getPartySubIdOrType());
                                to.setFspId(responsePartyIdInfo.getFspId());
                                to.setExtensionList(
                                    partiesResponse.getPartiesTypeIDPutResponse().getParty().getPartyIdInfo()
                                                   .getExtensionList());
                            }

                            if (partiesResponse.getPartiesTypeIDPutResponse().getParty().getPersonalInfo() != null) {

                                PartyComplexName partyComplexName =
                                    partiesResponse.getPartiesTypeIDPutResponse().getParty().getPersonalInfo()
                                                   .getComplexName();

                                to.setFirstName(partyComplexName.getFirstName());
                                to.setMiddleName(partyComplexName.getMiddleName());
                                to.setLastName(partyComplexName.getLastName());
                            }
                            if (partiesResponse.getPartiesTypeIDPutResponse().getParty().getName()!= null) {
                                to.setDisplayName(partiesResponse.getPartiesTypeIDPutResponse().getParty().getName());
                            }
                            List<Currency> supportedCurrenciesList =
                                partiesResponse.getPartiesTypeIDPutResponse().getParty().getSupportedCurrencies();

                            if (supportedCurrenciesList != null) {
                                to.setSupportedCurrencies(supportedCurrenciesList);
                            }

                            response.setTo(to);

                            response.setTo(to);

                            response.setAmountType(partiesRequest.getAmountType());
                            response.setCurrency(quoteCallOutput.getTransferAmount().getCurrency());
                            response.setAmount(quoteCallOutput.getTransferAmount().getAmount());
                            response.setPayeeReceiveAmount(quoteCallOutput
                                                               .getPayeeReceiveAmount());
                            response.setPayeeFspFee(quoteCallOutput
                                                        .getPayeeFspFee());
                            response.setPayeeFspCommission(quoteCallOutput
                                                               .getPayeeFspCommission());
                            response.setTransactionType(partiesRequest.getTransactionType());
                            response.setCurrentState(postTransfersOutput.getCurrentState());
                            response.setInitiatedTimestamp(postTransfersOutput.getInitiatedTimestamp());
                            response.setDirection(partiesResponse.getDirection());
                            response.setNote(partiesRequest.getNote());

                            LOG.info(
                                "AcceptQuote response from payer thitsaconect to payer:" +
                                    new Gson().toJson(response));

                            redisPayerService.removeFromMap(transferId);
                            return new ResponseEntity<>(response, HttpStatus.OK);

                        }
                    }

                }

            } catch (FspiopApiException e) {

                String errorCode =
                        ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYEE.getStatusCode().toString(); //Default error code

                ErrorResponse errorResponse = new ErrorResponse();

                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode;

                if (!Objects.isNull(e.getErrorResponse()) && !Objects.isNull(e.getErrorResponse()
                                                                              .getErrorInformation())) {

                    errorCode = e.getErrorResponse().getErrorInformation().getErrorCode();

                    errorResponse.setDetailedDescription(e.getErrorResponse()
                                                          .getErrorInformation()
                                                          .getErrorDescription());

                }

                String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(errorCode,
                                                                                         settings.getLocaleLanguage());

                try {
                    jsonNode = objectMapper.readTree(localizedMessage);
                } catch (JsonProcessingException ex) {
                    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                errorResponse.setStatusCode(jsonNode.get("errorInformation").get("statusCode").asText());
                errorResponse.setMessage(jsonNode.get("errorInformation").get("description").asText());
                errorResponse.setLocaleMessage(jsonNode.get("errorInformation").get("descriptionLocale").asText());


                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

            } catch (TimeoutException e) {

                ErrorResponse errorResponse = new ErrorResponse();

                String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(
                        ErrorCode.SERVER_TIMED_OUT.getStatusCode().toString(), settings.getLocaleLanguage());

                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode;

                try {
                    jsonNode = objectMapper.readTree(localizedMessage);
                } catch (JsonProcessingException ex) {
                    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                errorResponse.setStatusCode(jsonNode.get("errorInformation").get("statusCode").asText());
                errorResponse.setMessage(jsonNode.get("errorInformation").get("description").asText());
                errorResponse.setLocaleMessage(jsonNode.get("errorInformation").get("descriptionLocale").asText());
                errorResponse.setDetailedDescription(e.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.REQUEST_TIMEOUT);

            } catch (ConnectException e) {

                ErrorResponse errorResponse = new ErrorResponse();

                String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(
                        ErrorCode.COMMUNICATION_ERROR.getStatusCode().toString(), settings.getLocaleLanguage());

                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode;

                try {
                    jsonNode = objectMapper.readTree(localizedMessage);
                } catch (JsonProcessingException ex) {
                    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
                }

                errorResponse.setStatusCode(jsonNode.get("errorInformation").get("statusCode").asText());
                errorResponse.setMessage(jsonNode.get("errorInformation").get("description").asText());
                errorResponse.setLocaleMessage(jsonNode.get("errorInformation").get("descriptionLocale").asText());
                errorResponse.setDetailedDescription("FSPIOP Connection Refused.");

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

            } catch (Exception e) {

                ErrorResponse errorResponse = new ErrorResponse();

                String errorCode =
                        ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYEE.getStatusCode().toString(); //default error code

                String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(errorCode,
                                                                                         settings.getLocaleLanguage());


                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode;

                try {
                    jsonNode = objectMapper.readTree(localizedMessage);
                } catch (JsonProcessingException ex) {
                    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                errorResponse.setStatusCode(jsonNode.get("errorInformation").get("statusCode").asText());
                errorResponse.setMessage(jsonNode.get("errorInformation").get("description").asText());
                errorResponse.setLocaleMessage(jsonNode.get("errorInformation").get("descriptionLocale").asText());
                errorResponse.setDetailedDescription(e.getMessage());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

            }

        }

        LOG.info("AcceptQuote API call response might be null or not accept.");
        redisPayerService.removeFromMap(transferId);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class AcceptPartyRequest implements Serializable {

        @NotNull
        @JsonProperty("acceptParty")
        private Boolean acceptParty;

        @NotBlank
        @JsonProperty("amount")
        private String amount;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class AcceptQuoteRequest implements Serializable {

        @NotNull
        @JsonProperty("acceptQuote")
        private Boolean acceptQuote;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Response implements Serializable {

        @JsonProperty("transferId")
        private String transferId;

        @JsonProperty("homeTransactionId")
        private String homeTransactionId;

        @JsonProperty("from")
        private FspParty from;

        @JsonProperty("to")
        private FspParty to;

        @JsonProperty("amountType")
        private AmountType amountType;

        @JsonProperty("currency")
        private Currency currency;

        @JsonProperty("amount")
        private String amount;

        @JsonProperty("payeeReceiveAmount")
        private Money payeeReceiveAmount;

        @JsonProperty("payeeFspFee")
        private Money payeeFspFee;

        @JsonProperty("payeeFspCommission")
        private Money payeeFspCommission;

        @JsonProperty("transactionType")
        private TransactionType transactionType;

        @JsonProperty("currentState")
        private StateEnum currentState;

        @JsonProperty("initiatedTimestamp")
        private String initiatedTimestamp;

        @JsonProperty("direction")
        private String direction;

        @JsonProperty("note")
        private String note;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FspParty {

        @JsonProperty("type")
        private String type;

        @JsonProperty("idType")
        private PartyIdType idType;

        @JsonProperty("idValue")
        private String idValue;

        @JsonProperty("idSubValue")
        private String idSubValue;

        @JsonProperty("name")
        private String displayName;

        @JsonProperty("firstName")
        private String firstName;

        @JsonProperty("middleName")
        private String middleName;

        @JsonProperty("lastName")
        private String lastName;

        @JsonProperty("dateOfBirth")
        private String dateOfBirth;

        @JsonProperty("merchantClassificationCode")
        private String merchantClassificationCode;

        @JsonProperty("fspId")
        private String fspId;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

        @JsonProperty("supportedCurrencies")
        private List<Currency> supportedCurrencies;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class RejectResponse implements Serializable {

        @JsonProperty("acceptParty")
        private String acceptParty;

        @JsonProperty("acceptQuote")
        private String acceptQuote;

    }

}
