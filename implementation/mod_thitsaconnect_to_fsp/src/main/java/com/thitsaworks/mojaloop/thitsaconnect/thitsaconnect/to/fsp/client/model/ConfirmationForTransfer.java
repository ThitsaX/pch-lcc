package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesIDPutResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

public class ConfirmationForTransfer {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request implements Serializable {

        @JsonProperty("transferId")
        private String transferId;

        @JsonProperty("payeeParty")
        private Party payeeParty;

        @JsonProperty("payerParty")
        private Party payerParty;

        @JsonProperty("isRemittence")
        private boolean isRemittence;

        @JsonProperty("note")
        private String note;

        @JsonProperty("quotesIDPutResponse")
        private QuotesIDPutResponse quotesIDPutResponse;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {

        @JsonProperty("home_transaction_id")
        private String homeTransactionId;

        @JsonProperty("error")
        private ErrorInformation error;

    }

}
