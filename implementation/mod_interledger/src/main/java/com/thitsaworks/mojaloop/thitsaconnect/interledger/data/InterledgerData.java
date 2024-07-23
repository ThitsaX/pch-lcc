package com.thitsaworks.mojaloop.thitsaconnect.interledger.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterledgerData {

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("quoteId")
    private String quoteId;

    @JsonProperty("payee")
    private Party payee;

    @JsonProperty("payer")
    private Party payer;

    @JsonProperty("amount")
    private Money amount;

    @JsonProperty("transactionType")
    private TransactionType transactionType;

    @JsonProperty("note")
    private String note;

}
