package com.thitsaworks.mojaloop.thitsaconnect.component.mojaloop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorInformationResponse implements Serializable {

    private ErrorInformation errorInformation;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class ErrorInformation implements Serializable {

        private String statusCode;
        private String message;
        private String localeMessage;
        private String detailedDescription;


    }


}
