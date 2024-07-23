package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.controller;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransferState;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersIDPatchResponse;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisPayeeService;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayeeOutput;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.FspClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.ConfirmationForTransfer;
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
public class PatchTransferController {

    private static final Logger LOG = LoggerFactory.getLogger(PatchTransferController.class);

    @Autowired
    private RedisPayeeService redisPayeeService;

    @Autowired
    private Executor taskExecutor;

    @Autowired
    FspClient fspClient;

    @RequestMapping(
        value = "/transfers/{id}",
        method = RequestMethod.PATCH)
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
                                        @Valid @RequestBody TransfersIDPatchResponse request) {

        LOG.info("Patch Transfer request info from Hub to Payee thitsaconnect:" + new Gson().toJson(request));

        HubToPayeeOutput redisOutput = redisPayeeService.getDataWithTransferId(transferId);
        this.taskExecutor.execute(() -> {



            ConfirmationForTransfer.Request confirmationRequest = new ConfirmationForTransfer.Request();
            confirmationRequest.setTransferId(transferId);
            confirmationRequest.setQuotesIDPutResponse(redisOutput.getQuotesIDPutResponse());
            confirmationRequest.setPayeeParty(redisOutput.getPayeeParty());
            confirmationRequest.setPayerParty(redisOutput.getPayerParty());
            confirmationRequest.setRemittence(redisOutput.isRemittence());
            confirmationRequest.setNote(redisOutput.getNote());

            try {
                ConfirmationForTransfer.Response confirmationForTransfer =
                    this.fspClient.doConfirmationForTransfer(confirmationRequest);
            } catch (RetrofitRestApi.RestException e) {
                throw new RuntimeException(e);
            }

        });
        TransfersIDPatchResponse transfersIDPatchResponse = request;
        transfersIDPatchResponse.setTransferState(TransferState.COMMITTED);
        redisOutput.setTransfersIDPatchResponse(transfersIDPatchResponse);
        LOG.info("update the TransferState:" + new Gson().toJson(transfersIDPatchResponse));
        redisPayeeService.removeFromMap(transferId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
