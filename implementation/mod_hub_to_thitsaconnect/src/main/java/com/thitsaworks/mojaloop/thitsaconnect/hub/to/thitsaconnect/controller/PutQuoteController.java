package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.controller;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesIDPutResponse;
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

@RestController
public class PutQuoteController {

    private static final Logger LOG = LoggerFactory.getLogger(PutQuoteController.class);

    @Autowired
    private RedisPayerService redisPayerService;

    @Autowired
    private IValidateSignature validateSignature;

    @Autowired
    JwsConfiguration.Settings jwsConfigurationSettings;

    @RequestMapping(value = "/quotes/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Void> execute(@RequestHeader("Content-Type") String contentType,
                                        @RequestHeader("Date") String fspiopDate,
                                        @RequestHeader("FSPIOP-Source") String fspiopSource,
                                        @RequestHeader("FSPIOP-Destination") String fspiopDestination,
                                        // @RequestHeader("fspiop-encryption") String fspiopEncryption,
                                        @RequestHeader(value = "fspiop-signature", required = false) String fspiopSignature,
                                        @RequestHeader("fspiop-uri") String fspiopUri,
                                        @RequestHeader("fspiop-http-method") String fspiopHttpMethod,
                                        @PathVariable("id") String quoteId,
                                        @Valid @RequestBody QuotesIDPutResponse request) throws Exception {

        LOG.info("Put Quote request info from Hub to Payer thitsaconnect quoteId:" + quoteId);
        LOG.info("Put Quote request info from Hub to Payer thitsaconnect request:" + new Gson().toJson(request));


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

            HubToPayerOutput hubToPayerOutput = redisPayerService.getDataWithTransferId(quoteId);

            if (hubToPayerOutput == null) {

                LOG.error("Put Quote Redis Data Not Found with ID: " + quoteId);

            } else {

                HubToPayerOutput.PostQuoteOutput postQuoteOutput = new HubToPayerOutput.PostQuoteOutput();
                postQuoteOutput.setQuotesIDPutResponse(request);
                postQuoteOutput.setCurrentState(StateEnum.WAITING_FOR_QUOTE_ACCEPTANCE);
                postQuoteOutput.setInitiatedTimestamp(
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME));

                hubToPayerOutput.setPostQuoteOutput(postQuoteOutput);
                redisPayerService.publishData(quoteId, hubToPayerOutput);

            }

            return ResponseEntity.status(HttpStatus.OK).build();

        }
        else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}
