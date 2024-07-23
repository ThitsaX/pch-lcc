package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.GeoCode;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

public class ReservationForTransfer {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request implements Serializable {

        @JsonProperty("transferId")
        private String transferId;

        @JsonProperty("quote")
        private Quote quote;

        @JsonProperty("from")
        private FspParty from;

        @JsonProperty("to")
        private FspParty to;

        @JsonProperty("amountType")
        private AmountType amountType;

        @JsonProperty("currency")
        private Currency currency;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("transactionType")
        private String transactionType;

        @JsonProperty("ilpPacket")
        private IlpPacket ilpPacket;

        @JsonProperty("note")
        private String note;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Quote {

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

        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class IlpPacket {

            @JsonProperty("data")
            private Data data;

        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {

            @JsonProperty("quoteId")
            private String quoteId;

            @JsonProperty("transactionId")
            private String transactionId;

            @JsonProperty("payer")
            private Party payer;

            @JsonProperty("payee")
            private Party payee;

            @JsonProperty("amount")
            private Money amount;

            @JsonProperty("transactionType")
            private TransactionType transactionType;

        }

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {

        @JsonProperty("homeTransactionId")
        private String homeTransactionId;

        @JsonProperty("error")
        private ErrorInformation error;

    }

}
