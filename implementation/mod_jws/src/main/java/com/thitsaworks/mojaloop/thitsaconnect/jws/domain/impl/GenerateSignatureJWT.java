package com.thitsaworks.mojaloop.thitsaconnect.jws.domain.impl;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.thitsaworks.mojaloop.thitsaconnect.jws.RSAKeyProvider;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IGenerateSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class GenerateSignatureJWT implements IGenerateSignature {

    private static final Logger logger = Logger.getLogger(GenerateSignature.class.getName());

    @Autowired
    private RSAKeyProvider rsaKeyProvider;

    @Override
    public Output execute(Input input)
            throws Exception {

        RSAPublicKey publicKey = this.rsaKeyProvider.getRSAPublicKeyFromPEM(
                "C:\\NNEWORKING\\Mojaloop\\sdk-standard-components\\test\\unit\\data\\jwsValidationKey.pem");
        RSAPrivateKey privateKey = this.rsaKeyProvider.getRSAPrivateKeyFromPEM(
                "C:\\NNEWORKING\\Mojaloop\\sdk-standard-components\\test\\unit\\data\\jwsSigningKey.pem");

        JWSSigner signer = new RSASSASigner(privateKey);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("subject")
                .issuer("issuer")
                .expirationTime(new Date(System.currentTimeMillis() + 3600 * 1000))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

        signedJWT.sign(signer);

        String jwtString = signedJWT.serialize();

        System.out.println("Generated JWS token: " + jwtString);

        JWSObject jwsObject = JWSObject.parse(jwtString);
        JWSVerifier verifier = new RSASSAVerifier(publicKey);

//        if (jwsObject.verify(verifier)) {
//            System.out.println("Token is valid");
//        } else {
//            System.out.println("Token is not valid");
//        }

        String[] parts = jwtString.split("\\.");
        String protectedHeaderBase64 = parts[0];
        String payloadBase64 = parts[1];
        String signature = parts[2];

        Map<String, String> signatureObject = new HashMap<>();
        signatureObject.put("protectedHeader", protectedHeaderBase64.replace("\"", ""));
        signatureObject.put("payload", payloadBase64.replace("\"", ""));
        signatureObject.put("signature", signature.replace("\"", ""));

//        Output.SignatureObject signatureObjectOutput =
//                new Output.SignatureObject(protectedHeaderBase64.replace("\"", ""), payloadBase64.replace("\"", ""),
//                        signature.replace("\"", ""));

        return new Output("signatureObjectOutput");
    }

}
