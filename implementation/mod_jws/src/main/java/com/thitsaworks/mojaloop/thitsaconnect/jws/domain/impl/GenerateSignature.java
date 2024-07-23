package com.thitsaworks.mojaloop.thitsaconnect.jws.domain.impl;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.jws.RSAKeyProvider;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IGenerateSignature;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GenerateSignature implements IGenerateSignature {

    private static final Logger LOG = Logger.getLogger(GenerateSignature.class.getName());

    @Autowired
    private RSAKeyProvider rsaKeyProvider;

    private static final String SIGNATURE_ALGORITHM = "RS256";

    private static final Pattern uriRegex = Pattern.compile(
            "(?:^.*)(\\/(participants|parties|quotes|bulkQuotes|transfers|bulkTransfers|transactionRequests|thirdpartyRequests|authorizations|consents|consentRequests|fxQuotes|fxTransfers|)(\\/.*?)*)$");

    @Override
    public Output execute(Input input)
            throws IllegalArgumentException, JWTCreationException, IOException, InvalidKeySpecException,
            NoSuchAlgorithmException {

        String privateFileName = input.getFspiopSource() + ".pem";
        PrivateKey privateKey = rsaKeyProvider.readPKCS8PrivateKey(
                privateFileName);

        String payload = input.getBody();

        String uri = input.getFspiopUri();

        if (payload == null) {
            throw new IllegalArgumentException("Cannot sign with no body");
        }

        Matcher uriMatches = uriRegex.matcher(uri);
        if (!uriMatches.matches() || uriMatches.groupCount() < 2) {
            throw new IllegalArgumentException(String.format("URI not valid for protected header: %s", uri));
        }

        Map<String, Object> protectedHeaderObject = new HashMap<>();
        protectedHeaderObject.put("alg", SIGNATURE_ALGORITHM);
        protectedHeaderObject.put("FSPIOP-URI", input.getFspiopUri());
        protectedHeaderObject.put("FSPIOP-HTTP-Method", input.getFspiopHttpMethod().toString().toUpperCase());
        protectedHeaderObject.put("FSPIOP-Source", input.getFspiopSource());

        if (input.getFspiopDestination() != null) {
            protectedHeaderObject.put("FSPIOP-Destination", input.getFspiopDestination());
        }

        if (input.getDate() != null) {
            protectedHeaderObject.put("Date", input.getDate());
        }

        String token = Jwts.builder()
                .setHeader(protectedHeaderObject)
                .setPayload(input.getBody())
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();

        String[] parts = token.split("\\.");
        String protectedHeaderBase64 = parts[0];
        String payloadBase64 = parts[1];
        String signature = parts[2];

        Map<String, String> signatureObject = new LinkedHashMap<>();
        signatureObject.put("protectedHeader", protectedHeaderBase64.replace("\"", ""));
        signatureObject.put("payload", payloadBase64.replace("\"", ""));
        signatureObject.put("signature", signature.replace("\"", ""));

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonToken = objectMapper.writeValueAsString(signatureObject);
        LOG.info("jsonToken:" + token);

        return new Output(jsonToken);
    }

}
