package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.InterledgerData;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.ILPDataPacket;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.BuildPrepareIlpPacket;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.GenerateFulfillment;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.ValidateCondition;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerFulfillment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class ValidateConditionBean implements ValidateCondition {

    @Autowired
    private GenerateFulfillment generateFulfillment;

    private static final Logger LOG = LoggerFactory.getLogger(BuildPrepareIlpPacket.class);

    @Override
    public Output execute(Input input) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        if (input.getFulfillment() != null) {

            InterledgerFulfillment fulfillment =
                    InterledgerFulfillment.of(Base64.getUrlDecoder().decode(input.getFulfillment()));

            boolean isConditionValid = fulfillment.validateCondition(
                    InterledgerCondition.of(Base64.getUrlDecoder().decode(input.getCondition())));

            return new Output(isConditionValid);
        }

        String ilpDataJson = new String(input.getIlpPreparePacket().getData());

        ObjectMapper mapper = new ObjectMapper();

        InterledgerData interledgerData = mapper.readValue(ilpDataJson, InterledgerData.class);

        ILPDataPacket ilpDataPacket =
                new ILPDataPacket().amount(interledgerData.getAmount()).address(interledgerData.getPayee())
                                   .data(interledgerData);

        byte[] base64EncodedIlpData = Base64.getUrlEncoder().encode(new Gson().toJson(ilpDataPacket).getBytes());

        InterledgerFulfillment fulfillment =
                this.generateFulfillment.execute(new GenerateFulfillment.Input(base64EncodedIlpData)).getFulfillment();

        boolean isConditionValid = fulfillment.validateCondition(
                InterledgerCondition.of(Base64.getDecoder().decode(input.getCondition())));

        return new Output(isConditionValid);
    }

}
