package com.thitsaworks.mojaloop.thitsaconnect.jws.domain.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.thitsaworks.mojaloop.thitsaconnect.jws.RSAKeyProvider;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IValidateSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class ValidateSignature implements IValidateSignature {

    private static final Logger LOG = Logger.getLogger(ValidateSignature.class.getName());

    @Autowired
    private RSAKeyProvider rsaKeyProvider;

    private static final String SIGNATURE_ALGORITHM = "RS256";

    @Override
    public Output execute(Input input) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(input.getSignature());

        String protectedHeader = jsonNode.get("protectedHeader").asText();
        String payload = jsonNode.get("payload").asText();
        String signature = jsonNode.get("signature").asText();


        String fspiopSignatureString = protectedHeader + "." + payload + "." + signature;

        boolean bIsValid = true;

        try {

            PublicKey secret = this.rsaKeyProvider.readX509PublicKey(input.getFspiopSource() + "-pub.pem");

            String jws = fspiopSignatureString
                              .replace("Bearer ", "");

            SignedJWT signedJWT = SignedJWT.parse(jws);
            JWSHeader header = signedJWT.getHeader();

            if (!header.getAlgorithm().getName().equals(SIGNATURE_ALGORITHM)) {

                bIsValid = false;
                throw new Exception("Invalid signature algorithm");
            }

            // check protected header has all required fields and matches actual incoming headers
            this.validateProtectedHeader(input, header);

            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) secret);
            if (!signedJWT.verify(verifier)) {

                bIsValid = false;
                throw new Exception("Invalid signature");
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String decodedPayload = new String(decoder.decode(payload));

            if (!decodedPayload.equals(input.getRequestBody())) {

                bIsValid = false;
                throw new Exception("Invalid payload");
            }

            return new Output(true);

        } catch (JOSEException e) {

            LOG.severe("Error validating JWS: " + e.getMessage());

            throw new Exception("Error validating JWS");

        }

    }

    private void validateProtectedHeader(Input input, JWSHeader jwsHeader) {

        Map<String, Object> decodedProtectedHeader = jwsHeader.getCustomParams();

        // check FSPIOP-URI is present and matches
        if (!decodedProtectedHeader.containsKey("FSPIOP-URI")) {

            throw new IllegalArgumentException(
                    "Decoded protected header does not contain required FSPIOP-URI element: " + decodedProtectedHeader);
        }
        if (input.getFspiopUri().isEmpty()) {

            throw new IllegalArgumentException("FSPIOP-URI HTTP header not present in request headers: " + input);
        }
        if (!decodedProtectedHeader.get("FSPIOP-URI").equals(input.getFspiopUri())) {

            throw new IllegalArgumentException("FSPIOP-URI HTTP request header value: " + input.getFspiopUri() +
                    " does not match protected header value: " + decodedProtectedHeader.get("FSPIOP-URI"));
        }

        // check FSPIOP-Source is present and matches
        if (!decodedProtectedHeader.containsKey("FSPIOP-Source")) {

            throw new IllegalArgumentException(
                    "Decoded protected header does not contain required FSPIOP-Source element: " +
                            decodedProtectedHeader);
        }
        if (input.getFspiopSource().isEmpty()) {

            throw new IllegalArgumentException("FSPIOP-Source HTTP header not present in request headers: " + input);
        }
        if (!decodedProtectedHeader.get("FSPIOP-Source").equals(input.getFspiopSource())) {

            throw new IllegalArgumentException("FSPIOP-Source HTTP request header value: " + input.getFspiopSource() +
                    " does not match protected header value: " + decodedProtectedHeader.get("FSPIOP-Source"));
        }

        // if we have an HTTP fspiop-destination header it should also be in the protected header and the values should match exactly
        if (!input.getFspiopDestination().isEmpty() && !decodedProtectedHeader.containsKey("FSPIOP-Destination")) {

            throw new IllegalArgumentException(
                    "HTTP fspiop-destination header is present but is not present in protected header: " +
                            decodedProtectedHeader);
        }
        if (decodedProtectedHeader.containsKey("FSPIOP-Destination") && input.getFspiopDestination().isEmpty()) {

            throw new IllegalArgumentException(
                    "FSPIOP-Destination header is present in protected header but not in HTTP request: " + input);
        }
        if (!input.getFspiopDestination().isEmpty() &&
                !input.getFspiopDestination().equals(decodedProtectedHeader.get("FSPIOP-Destination"))) {

            throw new IllegalArgumentException("HTTP FSPIOP-Destination header: " + input.getFspiopDestination() +
                    " does not match protected header FSPIOP-Destination value: " +
                    decodedProtectedHeader.get("FSPIOP-Destination"));
        }

        // if we have a Date field in the protected header it must be present in the HTTP header and the values should match exactly
        if (decodedProtectedHeader.containsKey("Date") && input.getDate().isEmpty()) {

            throw new IllegalArgumentException(
                    "Date header is present in protected header but not in HTTP request: " + input);
        }
        if (decodedProtectedHeader.containsKey("Date") && !input.getDate().equals(decodedProtectedHeader.get("Date"))) {

            throw new IllegalArgumentException(
                    "HTTP date header: " + input.getDate() + " does not match protected header Date value: " +
                            decodedProtectedHeader.get("Date"));
        }

        // check FSPIOP-HTTP-Method is present and matches
        if (!decodedProtectedHeader.containsKey("FSPIOP-HTTP-Method")) {

            throw new IllegalArgumentException(
                    "Decoded protected header does not contain required FSPIOP-HTTP-Method element: " +
                            decodedProtectedHeader);
        }
        if (input.getFspiopHttpMethod().isEmpty()) {

            throw new IllegalArgumentException(
                    "FSPIOP-HTTP-Method HTTP header not present in request headers: " + input);
        }
        if (!decodedProtectedHeader.get("FSPIOP-HTTP-Method").equals(input.getFspiopHttpMethod())) {

            throw new IllegalArgumentException(
                    "FSPIOP-HTTP-Method HTTP request header value: " + input.getFspiopHttpMethod() +
                            " does not match protected header value: " +
                            decodedProtectedHeader.get("FSPIOP-HTTP-Method"));
        }
    }

}
