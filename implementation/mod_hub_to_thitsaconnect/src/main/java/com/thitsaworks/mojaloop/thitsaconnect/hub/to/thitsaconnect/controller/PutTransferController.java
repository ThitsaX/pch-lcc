package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.controller;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisPayerService;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayerOutput;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.StateEnum;
import com.thitsaworks.mojaloop.thitsaconnect.jws.JwsConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IValidateSignature;
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
import java.util.concurrent.Executor;

@RestController
public class PutTransferController {

    private static final Logger LOG = LoggerFactory.getLogger(PutTransferController.class);

    @Autowired
    private RedisPayerService redisPayerService;

    @Autowired
    private IValidateSignature validateSignature;

    @Autowired
    private Executor taskExecutor;

    @Autowired
    JwsConfiguration.Settings jwsConfigurationSettings;

    @RequestMapping(
        value = "/transfers/{id}",
        method = RequestMethod.PUT)
    public ResponseEntity<Void> execute(@RequestHeader("Content-Type") String contentType,
                                        @RequestHeader("Date") String fspiopDate,
                                        @RequestHeader("FSPIOP-Source") String fspiopSource,
                                        @RequestHeader("FSPIOP-Destination") String fspiopDestination,
                                        // @RequestHeader("fspiop-encryption") String fspiopEncryption,
                                        @RequestHeader(
                                            value = "fspiop-signature",
                                            required = false) String fspiopSignature,
                                        @RequestHeader("fspiop-uri") String fspiopUri,
                                        @RequestHeader("fspiop-http-method") String fspiopHttpMethod,
                                        @PathVariable("id") String transferId,
                                        @Valid @RequestBody TransfersIDPutResponse request) throws Exception {

        LOG.info("Put Transfer request info from Hub to Payer thitsaconnect transferId:" + transferId);
        LOG.info("Put Transfer request info from Hub to Payer thitsaconnect request:" + new Gson().toJson(request));

        this.taskExecutor.execute(() -> {
            HubToPayerOutput hubToPayerOutput = redisPayerService.getDataWithTransferId(transferId);

            if (hubToPayerOutput == null) {

                LOG.error("Put Transfer Error Redis Data Not Found with ID: " + transferId);

            } else {

                HubToPayerOutput.PostTransfersOutput
                    postTransfersOutput =
                    new HubToPayerOutput.PostTransfersOutput();
                postTransfersOutput.setTransfersIDPutResponse(request);
                postTransfersOutput.setCurrentState(StateEnum.COMPLETED);
                postTransfersOutput.setInitiatedTimestamp(
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME));

                hubToPayerOutput.setPostTransfersOutput(postTransfersOutput);
                redisPayerService.publishData(transferId, hubToPayerOutput);
            }

        });

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
