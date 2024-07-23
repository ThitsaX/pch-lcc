package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

public class Lookup {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request implements Serializable {

        @JsonProperty("idType")
        private PartyIdType idType;

        @JsonProperty("idValue")
        private String idValue;

        @JsonProperty("idSubValue")
        private String idSubValue;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {

        @JsonProperty("type")
        private String type;

        @JsonProperty("idType")
        private PartyIdType idType;

        @JsonProperty("idValue")
        private String idValue;

        @JsonProperty("idSubValue")
        private String idSubValue;

        @JsonProperty("displayName")
        private String displayName;

        @JsonProperty("firstName")
        private String firstName;

        @JsonProperty("middleName")
        private String middleName;

        @JsonProperty("lastName")
        private String lastName;

        @JsonProperty("dateOfBirth")
        private String dateOfBirth;

        @JsonProperty("merchantClassificationCode")
        private String merchantClassificationCode;

        @JsonProperty("fspId")
        private String fspId;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

        @JsonProperty("error")
        private ErrorInformation error;

        private List<Currency> supportedCurrencies;

    }

}
