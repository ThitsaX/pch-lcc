package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FspErrorResponse {

    private String developerMessage;

    private String httpStatusCode;

    private String defaultUserMessage;

    private Error[] errors;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Error {

        private String developerMessage;

        private String defaultUserMessage;

    }

}
