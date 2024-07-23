package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.controller;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartiesTypeIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisPayerService;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayerOutput;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.StateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class PutPartyController {

    private static final Logger LOG = LoggerFactory.getLogger(PutPartyController.class);

    @Autowired
    private RedisPayerService redisPayerService;

    @RequestMapping(value = "/parties/{type}/{id}/{subId}", method = RequestMethod.PUT)
    public ResponseEntity<Void> execute(@RequestHeader("Content-Type") String contentType,
                                        @RequestHeader("Date") String fspiopDate,
                                        @RequestHeader("FSPIOP-Source") String fspiopSource,
                                        @RequestHeader("FSPIOP-Destination") String fspiopDestination,
//            @RequestHeader("FSPIOP-Encryption") String fspiopEncryption,
//            @RequestHeader("FSPIOP-Signature") String fspiopSignature,
//            @RequestHeader("FSPIOP-URI") String fspiopURI,
//            @RequestHeader("FSPIOP-HTTP-METHOD") String fspiopHttpMethod,
                                        @PathVariable("type") PartyIdType idType, @PathVariable("id") String idValue,
                                        @PathVariable("subId") String idSubValue,
                                        @Valid @RequestBody PartiesTypeIDPutResponse request) {

        LOG.info("Put Party request info from Hub to Payer thitsaconnect:" + new Gson().toJson(request));

        HubToPayerOutput partiesResponse = new HubToPayerOutput();

        HubToPayerOutput.PartiesResponse partiesResponseData = new HubToPayerOutput.PartiesResponse();
        partiesResponseData.setPartiesTypeIDPutResponse(request);
        partiesResponseData.setCurrentState(StateEnum.WAITING_FOR_PARTY_ACCEPTANCE);
        partiesResponseData.setInitiatedTimestamp(
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME));
        partiesResponseData.setDirection("OUTBOUND");

        partiesResponse.setPartiesResponse(partiesResponseData);

        LOG.info("pub topic:"+fspiopDestination + fspiopSource+ idType + idValue +idSubValue);

        redisPayerService.publishFirstCallData( fspiopDestination +  idType + idValue + idSubValue,
                partiesResponse);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(value = "/parties/{type}/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Void> execute(@RequestHeader("Content-Type") String contentType,
                                        @RequestHeader("Date") String fspiopDate,
                                        @RequestHeader("FSPIOP-Source") String fspiopSource,
                                        @RequestHeader("FSPIOP-Destination") String fspiopDestination,
//            @RequestHeader("FSPIOP-Encryption") String fspiopEncryption,
//            @RequestHeader("FSPIOP-Signature") String fspiopSignarture,
//            @RequestHeader("FSPIOP-URI") String fspiopURI,
//            @RequestHeader("FSPIOP-HTTP-METHOD") String fspiopHttpMethod,
                                        @PathVariable("type") PartyIdType idType, @PathVariable("id") String idValue,
                                        @Valid @RequestBody PartiesTypeIDPutResponse request) {

        LOG.info("Put Party request info from Hub to Payer thitsaconnect:" + new Gson().toJson(request));

        HubToPayerOutput partiesResponse = new HubToPayerOutput();

        HubToPayerOutput.PartiesResponse partiesResponseData = new HubToPayerOutput.PartiesResponse();
        partiesResponseData.setPartiesTypeIDPutResponse(request);
        partiesResponseData.setCurrentState(StateEnum.WAITING_FOR_PARTY_ACCEPTANCE);
        partiesResponseData.setInitiatedTimestamp(
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME));
        partiesResponseData.setDirection("OUTBOUND");

        partiesResponse.setPartiesResponse(partiesResponseData);

        LOG.info("pub topic:"+fspiopDestination + fspiopSource+ idType + idValue);

        redisPayerService.publishFirstCallData( fspiopDestination + idType + idValue, partiesResponse);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
