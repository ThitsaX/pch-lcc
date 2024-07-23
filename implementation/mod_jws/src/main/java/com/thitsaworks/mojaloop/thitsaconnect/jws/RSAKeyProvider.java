package com.thitsaworks.mojaloop.thitsaconnect.jws;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RSAKeyProvider {

    public PublicKey readX509PublicKey(String publicKeyFileName) throws IOException, InvalidKeySpecException,
            NoSuchAlgorithmException {

        try (InputStream inputStream = getClass().getResourceAsStream("/" + publicKeyFileName)) {
            if (inputStream == null) {
                throw new IOException("File not found: " + publicKeyFileName);
            }

            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            StringBuilder keyPem = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                keyPem.append((char) ch);
            }

            String key = keyPem.toString();

            String publicKeyPEM = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PUBLIC KEY-----", "");

            byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return keyFactory.generatePublic(keySpec);
        }
    }

    public PrivateKey readPKCS8PrivateKey(String privateKeyFileName) throws IOException, InvalidKeySpecException,
            NoSuchAlgorithmException {

        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try (InputStream inputStream = getClass().getResourceAsStream("/" + privateKeyFileName)) {
            if (inputStream == null) {
                throw new IOException("File not found: " + privateKeyFileName);
            }

            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            StringBuilder keyPem = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                keyPem.append((char) ch);
            }

            String key = keyPem.toString();

            String privateKeyPEM = key
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END RSA PRIVATE KEY-----", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return keyFactory.generatePrivate(keySpec);
        }
    }

    public RSAPublicKey getRSAPublicKeyFromPEM(String filePath) throws Exception {

        try (FileReader fileReader = new FileReader(filePath);
             PEMParser pemParser = new PEMParser(fileReader)) {

            PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();
            KeyPair keyPair = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);

            return (RSAPublicKey) keyPair.getPublic();
        }
    }

    public RSAPrivateKey getRSAPrivateKeyFromPEM(String filePath) throws Exception {

        try (FileReader fileReader = new FileReader(filePath);
             PEMParser pemParser = new PEMParser(fileReader)) {

            PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();
            KeyPair keyPair = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);

            return (RSAPrivateKey) keyPair.getPrivate();
        }
    }

}
