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
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Extension;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyPersonalInfo;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionType;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisPayerService;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayerOutput;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.StateEnum;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.GetPartiesById;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.exception.FspiopApiException;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.support.TransferIdGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RestController
public class PostSecuredSendMoneyController {

    private static final Logger LOG = LoggerFactory.getLogger(PostSecuredSendMoneyController.class);

    @Autowired
    private ThitsaconnectToHubClient thitsaconnectToHubClient;

    @Autowired
    private RedisPayerService redisPayerService;

    @Autowired
    FspToThitsaconnectConfiguration.Settings settings;

    @RequestMapping(
        value = "/secured/sendMoney",
        method = RequestMethod.POST)
    public ResponseEntity<Object> execute(@Valid @RequestBody PostSecuredSendMoneyController.Request request)
        throws JsonProcessingException {

        String topicToListen =
            request.getFrom().getFspId() + request.getTo().getIdType() +
                request.getTo().getIdValue();

        if (request.getTo().getIdSubValue() != null && !request.getTo().getIdSubValue().isEmpty()) {
            topicToListen = topicToListen + request.getTo().getIdSubValue();
        }
        LOG.info("sub topic:" + topicToListen);
        try {

            GetPartiesById.Response getPartiesResponse =
                this.thitsaconnectToHubClient.getParties(request.from.fspId,
                                                         request.to.fspId,
                                                         "RS256",
                                                         "/parties",
                                                         "GET",
                                                         request.to.idValue,
                                                         request.to.idType,
                                                         request.to.idSubValue,
                                                         request.currency);
            HubToPayerOutput redisOutput = redisPayerService.subscribe(topicToListen);
            redisPayerService.removeTopicFromMap(topicToListen);

            if (redisOutput == null ||
                (redisOutput.getPartiesResponse() == null && redisOutput.getErrorOutput() == null)) {
                LOG.info("GetParties no redis output obtained.");

                throw new TimeoutException("GetParties no redis output obtained.");
            }

            if (redisOutput.getErrorOutput() != null) {

                LOG.info("GetParties error response from Hub to payer thitsaconnect:" +
                             new Gson().toJson(redisOutput.getErrorOutput()));

                ErrorInformationResponse errorInformationResponse =
                    redisOutput.getErrorOutput().getErrorInformationResponse();

                if (errorInformationResponse == null) {

                    LOG.error("GetParties error response from Hub to payer thitsaconnect is null.");

                    throw new RuntimeException("GetParties error response from Hub to payer thitsaconnect is null.");
                }

                redisPayerService.removeTopicFromMap(topicToListen);

                throw new FspiopApiException(errorInformationResponse);

            } else {

                LOG.info("GetParties response from Hub to payer thitsaconnect:" +
                             new Gson().toJson(redisOutput.getPartiesResponse()));

                FspParty responseFrom = new FspParty();
                responseFrom.setType(request.getFrom().type);
                responseFrom.setIdType(request.getFrom().idType);
                responseFrom.setIdValue(request.getFrom().idValue);
                responseFrom.setIdSubValue(request.getFrom().idSubValue);
                responseFrom.setFspId(request.getFrom().fspId);

                FspParty responseTo = new FspParty();

                responseTo.setType(request.to.getType());

                HubToPayerOutput.PartiesResponse partiesResponse = redisOutput.getPartiesResponse();

                PartyIdInfo partyIdInfo = partiesResponse.getPartiesTypeIDPutResponse().getParty().getPartyIdInfo();
                PartyPersonalInfo partyPersonalInfo =
                    partiesResponse.getPartiesTypeIDPutResponse().getParty().getPersonalInfo();

                List<Currency>
                    supportedCurrenciesList =
                    partiesResponse.getPartiesTypeIDPutResponse().getParty().getSupportedCurrencies();

                if (partyIdInfo != null) {

                    responseTo.setIdType(partyIdInfo.getPartyIdType());
                    responseTo.setIdValue(partyIdInfo.getPartyIdentifier());
                    responseTo.setIdSubValue(partyIdInfo.getPartySubIdOrType());
                    responseTo.setFspId(partyIdInfo.getFspId());
                    responseTo.setExtensionList(partyIdInfo.getExtensionList());

                }

                if (supportedCurrenciesList != null) {
                    responseTo.setSupportedCurrencies(supportedCurrenciesList);
                }

                if (partyPersonalInfo != null) {

                    responseTo.setFirstName(partyPersonalInfo.getComplexName().getFirstName());
                    responseTo.setMiddleName(partyPersonalInfo.getComplexName().getMiddleName());
                    responseTo.setLastName(partyPersonalInfo.getComplexName().getLastName());

                }
                if ( partiesResponse.getPartiesTypeIDPutResponse().getParty().getName()!=null){
                    responseTo.setDisplayName(partiesResponse.getPartiesTypeIDPutResponse().getParty().getName());
                }

                String transferId = TransferIdGenerator.generateTransferId();
                redisOutput.getPartiesResponse().setTransferId(transferId);

                Response response = new Response();
                response.setTransferId(transferId);
                response.setHomeTransactionId(request.getHomeTransactionId());
                response.setFrom(responseFrom);
                response.setTo(responseTo);
                response.setAmountType(request.getAmountType());
                response.setCurrency(request.getCurrency());

                response.setTransactionType(request.getTransactionType());

                response.setCurrentState(partiesResponse.getCurrentState());
                response.setInitiatedTimestamp(partiesResponse.getInitiatedTimestamp());
                response.setDirection(partiesResponse.getDirection());
                response.setNote(request.getNote());

                Party payer = new Party();
                PartyIdInfo payerPartyIdInfo = new PartyIdInfo();
                payerPartyIdInfo.setPartyIdentifier(request.getFrom().idValue);
                payerPartyIdInfo.setPartySubIdOrType(request.getFrom().idSubValue);
                payerPartyIdInfo.setPartyIdType(request.getFrom().idType);
                payerPartyIdInfo.setFspId(request.getFrom().fspId);
                payer.setPartyIdInfo(payerPartyIdInfo);

                redisOutput.setPayerParty(new HubToPayerOutput.RedisPartyData(request.getFrom().type, payer));

                Party payee = new Party();
                PartyIdInfo payeePartyIdInfo = new PartyIdInfo();
                payeePartyIdInfo.setPartyIdentifier(request.getTo().idValue);
                payeePartyIdInfo.setPartySubIdOrType(request.getTo().idSubValue);
                payeePartyIdInfo.setPartyIdType(request.getTo().idType);
                payeePartyIdInfo.setFspId(request.getTo().fspId);
                payee.setPartyIdInfo(payeePartyIdInfo);
                payee.setSupportedCurrencies(supportedCurrenciesList);

                redisOutput.setPayeeParty(new HubToPayerOutput.RedisPartyData(request.getTo().type, payee));

                redisOutput.setPartiesRequest(
                    new HubToPayerOutput.PartiesRequest(request.getHomeTransactionId(),
                                                        request.getAmountType(),
                                                        request.getCurrency(),
                                                        request.getTransactionType(),
                                                        request.getNote()));

                redisPayerService.putToMap(transferId, redisOutput);

                LOG.info("GetParties response from payer thitsaconnect to payer:" + new Gson().toJson(response));

                return new ResponseEntity<>(response, HttpStatus.OK);

            }

        } catch (FspiopApiException e) {

            String errorCode = ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYER.getStatusCode().toString();

            if (e.getErrorResponse().getErrorInformation() != null &&
                e.getErrorResponse().getErrorInformation().getErrorCode() != null) {

                errorCode = e.getErrorResponse().getErrorInformation().getErrorCode();

            }

            String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(errorCode,
                                                                                     settings.getLocaleLanguage());

            ErrorResponse errorResponse = new ErrorResponse();

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
            errorResponse.setDetailedDescription(e.getErrorResponse().getErrorInformation().getErrorDescription());

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (TimeoutException e) {

            ErrorResponse errorResponse = new ErrorResponse();

            String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(
                ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYER.getStatusCode().toString(),
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

            return new ResponseEntity<>(errorResponse, HttpStatus.REQUEST_TIMEOUT);

        } catch (ConnectException e) {

            ErrorResponse errorResponse = new ErrorResponse();

            String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(
                ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYER.getStatusCode().toString(),
                settings.getLocaleLanguage());

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

            String localizedMessage = ErrorCode.getMojaloopErrorResponseByStatusCode(
                ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYER.getStatusCode().toString(),
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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Request implements Serializable {

        @NotBlank
        @JsonProperty("homeTransactionId")
        private String homeTransactionId;

        @NotNull
        @JsonProperty("from")
        private FspParty from;

        @NotNull
        @JsonProperty("to")
        private FspParty to;

        @NotNull
        @JsonProperty("amountType")
        private AmountType amountType;

        @NotNull
        @JsonProperty("currency")
        private Currency currency;

        @NotNull
        @JsonProperty("transactionType")
        private TransactionType transactionType;

        @JsonProperty("note")
        private String note;

        @JsonProperty("quoteRequestExtensions")
        private Extension quoteRequestExtensions;

        @JsonProperty("transferRequestExtensions")
        private Extension transferRequestExtensions;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

        @JsonInclude(JsonInclude.Include.NON_NULL)
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

        @NotNull
        @JsonProperty("idType")
        private PartyIdType idType;

        @NotBlank
        @JsonProperty("idValue")
        private String idValue;

        @NotNull
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

        @NotBlank
        @JsonProperty("fspId")
        private String fspId;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

        @JsonProperty("supportedCurrencies")
        private List<Currency> supportedCurrencies;

    }

}