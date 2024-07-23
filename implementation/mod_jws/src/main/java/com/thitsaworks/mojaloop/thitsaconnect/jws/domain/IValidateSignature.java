package com.thitsaworks.mojaloop.thitsaconnect.jws.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public interface IValidateSignature {

    @Getter
    @Setter
    @AllArgsConstructor
    class Input implements Serializable {

        private String alg;

        private String fspiopUri;

        private String fspiopSource;

        private String fspiopDestination;

        private String date;

        private String fspiopHttpMethod;

        private String signature;

        private String requestBody;

    }

    @Getter
    @AllArgsConstructor
    class Output {

        private Boolean isValid;

    }

    Output execute(Input input)
            throws Exception;
}
