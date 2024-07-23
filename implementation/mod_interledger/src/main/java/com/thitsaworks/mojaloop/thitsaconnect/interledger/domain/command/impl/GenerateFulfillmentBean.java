package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.InterledgerConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.BuildPrepareIlpPacket;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.GenerateFulfillment;
import org.interledger.core.InterledgerFulfillment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class GenerateFulfillmentBean implements GenerateFulfillment {

    @Autowired
    private InterledgerConfiguration.Settings settings;

    private static final Logger LOG = LoggerFactory.getLogger(BuildPrepareIlpPacket.class);

    @Override
    public Output execute(Input input) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException {

        // Step 1: Encode the secret and packet as base64
        String encodedSecret = encodeBase64Url(this.settings.getThitsaconnectKey());

        // Step 2: Create an HMAC object with the SHA-256 algorithm
        SecretKeySpec secretKey = new SecretKeySpec(encodedSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(secretKey);

        // Step 3: Update the HMAC object with the base64 encoded packet
        hmac.update(input.getBase64EncodedIlpDataPacket());

        // Step 4: Generate the HMAC signature
        byte[] preImage = hmac.doFinal();

        return new Output(InterledgerFulfillment.of(preImage));
    }

    private static String encodeBase64Url(String input) {

        byte[] base64Bytes = Base64.getUrlEncoder().encode(input.getBytes(StandardCharsets.UTF_8));
        return new String(base64Bytes, StandardCharsets.UTF_8)
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "");

    }

}
