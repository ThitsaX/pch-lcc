package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.controller;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.*;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.FspClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.Lookup;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubFromPayeeClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.exception.FspiopApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Query;

import java.net.ConnectException;
import java.util.concurrent.Executor;

@RestController
public class GetPartyController {

    private static final Logger LOG = LoggerFactory.getLogger(GetPartyController.class);

    @Autowired
    private FspClient fspClient;

    @Autowired
    private Executor taskExecutor;

    @Autowired
    private ThitsaconnectToHubFromPayeeClient thitsaconnectToHubFromPayeeClient;

    @RequestMapping(value = "/parties/{type}/{id}/{subId}", method = RequestMethod.GET)
    public ResponseEntity<Void> execute(@RequestHeader("fspiop-source") String fspiopSource,
                                        @RequestHeader("fspiop-destination") String fspiopDestination,
                                        @RequestHeader("date") String fspiopDate,
                                        @RequestHeader("accept") String accept,
                                        @RequestHeader("content-type") String contentType,
                                        @Query("currency") Currency currency, @PathVariable("type") PartyIdType idType,
                                        @PathVariable("id") String idValue, @PathVariable("subId") String idSubValue)
            throws FspiopApiException, ConnectException {

        Lookup.Request request = new Lookup.Request(idType, idValue, idSubValue);
        LOG.info("Get Parties request info from Hub to Payee thitsaconnect:" + new Gson().toJson(request));

        this.taskExecutor.execute(() -> {

            try {
                Lookup.Response lookupResponse = this.fspClient.doLookUp(request);
                LOG.info("LookUp response from Payee:" + new Gson().toJson(lookupResponse));

                if (lookupResponse == null) {
                    // TODO: How to response request time out to hub
                    ErrorInformation errorInformation = new ErrorInformation();
                    errorInformation.setErrorCode("");
                    errorInformation.setErrorDescription("");

                    ErrorInformationResponse errorInfoRequest = new ErrorInformationResponse();

                    errorInfoRequest.errorInformation(errorInformation);

                    LOG.info("Put Party Error Request from Payee thitsaconnect to Hub:" + new Gson().toJson(errorInfoRequest));

                    this.thitsaconnectToHubFromPayeeClient.putPartiesError(fspiopDestination, fspiopSource, currency,
                            fspiopDate, errorInfoRequest, idType, idValue, idSubValue, accept, contentType);

                } else if (lookupResponse.getError() != null) {

                    ErrorInformationResponse errorInfoRequest = new ErrorInformationResponse();

                    errorInfoRequest.errorInformation(lookupResponse.getError());

                    LOG.info("Put Party Error Request from Payee thitsaconnect to Hub:" + new Gson().toJson(errorInfoRequest));

                    this.thitsaconnectToHubFromPayeeClient.putPartiesError(fspiopDestination, fspiopSource, currency,
                            fspiopDate, errorInfoRequest, idType, idValue, idSubValue, accept, contentType);

                } else {


                    PartiesTypeIDPutResponse putPartyRequest = new PartiesTypeIDPutResponse();

                    PartyIdInfo partyIdInfo = new PartyIdInfo().partyIdType(lookupResponse.getIdType())
                            .partyIdentifier(lookupResponse.getIdValue())
                            .partySubIdOrType(lookupResponse.getIdSubValue())
                            .fspId(fspiopDestination)
                            .extensionList(lookupResponse.getExtensionList());

                    PartyComplexName partyComplexName = new PartyComplexName().firstName(lookupResponse.getFirstName())
                            .middleName(lookupResponse.getMiddleName())
                            .lastName(lookupResponse.getLastName());

                    PartyPersonalInfo personalInfo =
                            new PartyPersonalInfo().dateOfBirth(lookupResponse.getDateOfBirth()).complexName(partyComplexName);

                    Party party = new Party().partyIdInfo(partyIdInfo)
                            .merchantClassificationCode(lookupResponse.getMerchantClassificationCode())
                            .name(lookupResponse.getDisplayName()).personalInfo(personalInfo)
                           .supportedCurrencies(lookupResponse.getSupportedCurrencies());

                    putPartyRequest.party(party);

                    LOG.info("Put Party Request from Payee thitsaconnect to Hub:" + new Gson().toJson(putPartyRequest));

                    this.thitsaconnectToHubFromPayeeClient.putParties(fspiopDestination, fspiopSource, currency, fspiopDate,
                            putPartyRequest, accept, contentType);

                }

            } catch (Exception e) {

                throw new RuntimeException(e);
            }
        });

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();


    }

    @RequestMapping(value = "/parties/{type}/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> execute(@RequestHeader("fspiop-source") String fspiopSource,
                                     @RequestHeader("fspiop-destination") String fspiopDestination,
                                     @RequestHeader("date") String fspiopDate, @RequestHeader("accept") String accept,
                                     @RequestHeader("content-type") String contentType,
                                     @Query("currency") Currency currency, @PathVariable("type") PartyIdType idType,
                                     @PathVariable("id") String idValue) throws FspiopApiException, ConnectException {

        Lookup.Request request = new Lookup.Request(idType, idValue, idValue);

        LOG.info("Get Party request info from Hub to Payee:" + new Gson().toJson(request));
        this.taskExecutor.execute(() -> {

            try {
                Lookup.Response lookupResponse = this.fspClient.doLookUp(request);

                if (lookupResponse == null) {

                    ErrorInformation errorInformation = new ErrorInformation();
                    errorInformation.setErrorCode("");
                    errorInformation.setErrorDescription("");

                    ErrorInformationResponse errorInfoRequest = new ErrorInformationResponse();

                    errorInfoRequest.errorInformation(errorInformation);

                    LOG.info("Put Party Error Request from Payee thitsaconnect to Hub:" + new Gson().toJson(errorInfoRequest));

                    this.thitsaconnectToHubFromPayeeClient.putPartiesError(fspiopDestination, fspiopSource, currency,
                            fspiopDate, errorInfoRequest, idType, idValue, null, accept, contentType);

                } else if (lookupResponse.getError() != null) {

                    ErrorInformationResponse errorInfoRequest = new ErrorInformationResponse();

                    errorInfoRequest.errorInformation(lookupResponse.getError());

                    LOG.info("Put Party Error Request from Payee thitsaconnect to Hub:" + new Gson().toJson(errorInfoRequest));

                    this.thitsaconnectToHubFromPayeeClient.putPartiesError(fspiopDestination, fspiopSource, currency,
                            fspiopDate, errorInfoRequest, idType, idValue, null, accept, contentType);

                } else {

                    LOG.info("LookUp response from Payee:" + new Gson().toJson(lookupResponse));

                    PartiesTypeIDPutResponse putPartyRequest = new PartiesTypeIDPutResponse();

                    PartyIdInfo partyIdInfo = new PartyIdInfo().partyIdType(lookupResponse.getIdType())
                            .partyIdentifier(lookupResponse.getIdValue())
                            .fspId(fspiopDestination)
                            .extensionList(lookupResponse.getExtensionList());

                    PartyComplexName partyComplexName = new PartyComplexName().firstName(lookupResponse.getFirstName())
                            .middleName(lookupResponse.getMiddleName())
                            .lastName(lookupResponse.getLastName());

                    PartyPersonalInfo personalInfo =
                            new PartyPersonalInfo().dateOfBirth(lookupResponse.getDateOfBirth()).complexName(partyComplexName);

                    Party party = new Party().partyIdInfo(partyIdInfo)
                            .merchantClassificationCode(lookupResponse.getMerchantClassificationCode())
                            .name(lookupResponse.getDisplayName()).personalInfo(personalInfo)
                            .supportedCurrencies(lookupResponse.getSupportedCurrencies());


                    putPartyRequest.party(party);

                    LOG.info("Put Party Request from Payee thitsaconnect to Hub:" + new Gson().toJson(putPartyRequest));

                    this.thitsaconnectToHubFromPayeeClient.putParties(fspiopDestination, fspiopSource, currency, fspiopDate,
                            putPartyRequest, accept, contentType);

                }
            } catch (Exception e) {

                throw new RuntimeException(e);
            }
        });

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
