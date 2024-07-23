package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FspiopErrorResponse {

    private String status;

    private Error[] errors;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Error {

        private String instancePath;

        private String schemaPath;

        private Params params;

        private String message;

    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Params {

        private String type;

    }

}
