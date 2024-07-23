package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.controller;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
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
public class PutTransferErrorController {

    private static final Logger LOG = LoggerFactory.getLogger(PutTransferErrorController.class);

    @Autowired
    private RedisPayerService redisPayerService;

    @RequestMapping(value = "/transfers/{id}/error", method = RequestMethod.PUT)
    public ResponseEntity<Void> execute(@RequestHeader("Content-Type") String contentType,
                                        @RequestHeader("Date") String fspiopDate,
                                        @RequestHeader("FSPIOP-Source") String fspiopSource,
                                        @RequestHeader("FSPIOP-Destination") String fspiopDestination,
//                                        @RequestHeader("FSPIOP-Encryption") String fspiopEncryption,
//                                        @RequestHeader("FSPIOP-Signature") String fspiopSignature,
//                                        @RequestHeader("FSPIOP-URI") String fspiopURI,
//                                        @RequestHeader("FSPIOP-HTTP-METHOD") String fspiopHttpMethod,
                                        @PathVariable("id") String transferId,
                                        @Valid @RequestBody ErrorInformationResponse request) {

        LOG.info("Put Transfer error request info from Hub to Payer thitsaconnect transferId:" + transferId);
        LOG.info("Put Transfer error request info from Hub to Payer thitsaconnect request:" +
                new Gson().toJson(request));

        HubToPayerOutput hubToPayerOutput = redisPayerService.getDataWithTransferId(transferId);

        if (hubToPayerOutput == null) {

            LOG.error("Put Transfer Error Redis Data Not Found with ID: " + transferId);

        } else {

            HubToPayerOutput.ErrorOutput errorOutput = new HubToPayerOutput.ErrorOutput();
            errorOutput.setErrorInformationResponse(request);
            hubToPayerOutput.setErrorOutput(errorOutput);

            redisPayerService.publishData(transferId, hubToPayerOutput);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
