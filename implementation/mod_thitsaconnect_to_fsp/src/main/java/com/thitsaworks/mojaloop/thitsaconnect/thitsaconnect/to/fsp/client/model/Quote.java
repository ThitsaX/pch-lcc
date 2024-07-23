package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.GeoCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

public class Quote {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request implements Serializable {

        @JsonProperty("quoteId")
        private String quoteId;

        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("to")
        private FspParty to;

        @JsonProperty("from")
        private FspParty from;

        @JsonProperty("amountType")
        private AmountType amountType;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("currency")
        private Currency currency;

        @JsonProperty("feesAmount")
        private BigDecimal feesAmount;

        @JsonProperty("feesCurrency")
        private Currency feesCurrency;

        @JsonProperty("transactionType")
        private String transactionType;

        @JsonProperty("initiator")
        private String initiator;

        @JsonProperty("initiatorType")
        private String initiatorType;

        @JsonProperty("geoCode")
        private GeoCode geoCode;

        @JsonProperty("note")
        private String note;

        @JsonProperty("expiration")
        private String expiration;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {

        @JsonProperty("quoteId")
        private String quoteId;

        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("transferAmount")
        private BigDecimal transferAmount;

        @JsonProperty("transferAmountCurrency")
        private String transferAmountCurrency;

        @JsonProperty("payeeReceiveAmount")
        private BigDecimal payeeReceiveAmount;

        @JsonProperty("payeeReceiveAmountCurrency")
        private String payeeReceiveAmountCurrency;

        @JsonProperty("payeeFspFeeAmount")
        private BigDecimal payeeFspFeeAmount;

        @JsonProperty("payeeFspFeeAmountCurrency")
        private String payeeFspFeeAmountCurrency;

        @JsonProperty("payeeFspCommissionAmount")
        private BigDecimal payeeFspCommissionAmount;

        @JsonProperty("payeeFspCommissionAmountCurrency")
        private String payeeFspCommissionAmountCurrency;

        @JsonProperty("expiration")
        private String expiration;

        @JsonProperty("geoCode")
        private GeoCode geoCode;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

        @JsonProperty("error")
        private ErrorInformation error;

    }

}
