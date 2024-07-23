package com.thitsaworks.mojaloop.thitsaconnect.jws.domain;

import com.auth0.jwt.exceptions.JWTCreationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface IGenerateSignature {

    @Getter
    @Setter
    @AllArgsConstructor
    class Input implements Serializable {

        private String alg;

        private String fspiopUri;

        private String fspiopSource;

        private String fspiopDestination;

        private String date;

        private String body;

        private String fspiopHttpMethod;

    }

    @Getter
    @AllArgsConstructor
    class Output {

        private String signatureObject;


    }

    Output execute(Input input)
            throws Exception;

}
