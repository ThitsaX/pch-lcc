package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.controller;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisPayerService;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayerOutput;
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
public class PutPartyErrorController {

    private static final Logger LOG = LoggerFactory.getLogger(PutPartyErrorController.class);

    @Autowired
    private RedisPayerService redisPayerService;

    @RequestMapping(
        value = "/parties/{type}/{id}/{subId}/error",
        method = RequestMethod.PUT)
    public ResponseEntity<Void> execute(@RequestHeader("Content-Type") String contentType,
                                        @RequestHeader("Date") String fspiopDate,
                                        @RequestHeader("FSPIOP-Source") String fspiopSource,
                                        @RequestHeader("FSPIOP-Destination") String fspiopDestination,
//                                        @RequestHeader("FSPIOP-Encryption") String fspiopEncryption,
//                                        @RequestHeader("FSPIOP-Signature") String fspiopSignature,
//                                        @RequestHeader("FSPIOP-URI") String fspiopURI,
//                                        @RequestHeader("FSPIOP-HTTP-METHOD") String fspiopHttpMethod,
                                        @PathVariable("type") PartyIdType idType, @PathVariable("id") String idValue,
                                        @PathVariable("subId") String idSubValue,
                                        @Valid @RequestBody ErrorInformationResponse request) {

        LOG.info("Put Party error request info from Hub to Payer: " + new Gson().toJson(request));

        if (request.getErrorInformation() == null) {
            ErrorInformation errorInformation = new ErrorInformation();
            errorInformation.setErrorCode("5000");
            errorInformation.setErrorDescription("No Such Mojaloop Error Present");
            request.setErrorInformation(errorInformation);
        }

        HubToPayerOutput hubToPayerOutput = new HubToPayerOutput();
        HubToPayerOutput.ErrorOutput errorOutput = new HubToPayerOutput.ErrorOutput(request);
        hubToPayerOutput.setErrorOutput(errorOutput);

        redisPayerService.publishFirstCallData(fspiopDestination + idType + idValue + idSubValue,
                                               hubToPayerOutput);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(
        value = "/parties/{type}/{id}/error",
        method = RequestMethod.PUT)
    public ResponseEntity<Void> execute(@RequestHeader("Content-Type") String contentType,
                                        @RequestHeader("Date") String fspiopDate,
                                        @RequestHeader("FSPIOP-Source") String fspiopSource,
                                        @RequestHeader("FSPIOP-Destination") String fspiopDestination,
//                                        @RequestHeader("FSPIOP-Encryption") String fspiopEncryption,
//                                        @RequestHeader("FSPIOP-Signature") String fspiopSignarture,
//                                        @RequestHeader("FSPIOP-URI") String fspiopURI,
//                                        @RequestHeader("FSPIOP-HTTP-METHOD") String fspiopHttpMethod,
                                        @PathVariable("type") PartyIdType idType, @PathVariable("id") String idValue,
                                        @Valid @RequestBody ErrorInformationResponse request) {

        LOG.info("Put Party error request info from Hub to Payer: " + new Gson().toJson(request));

        if (request.getErrorInformation() == null) {
            ErrorInformation errorInformation = new ErrorInformation();
            errorInformation.setErrorCode("5000");
            errorInformation.setErrorDescription("No Such Mojaloop Error Present");
            request.setErrorInformation(errorInformation);
        }

        HubToPayerOutput hubToPayerOutput = new HubToPayerOutput();

        HubToPayerOutput.ErrorOutput errorOutput = new HubToPayerOutput.ErrorOutput(request);
        hubToPayerOutput.setErrorOutput(errorOutput);

        redisPayerService.publishFirstCallData(fspiopDestination + idType + idValue,
                                               hubToPayerOutput);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
