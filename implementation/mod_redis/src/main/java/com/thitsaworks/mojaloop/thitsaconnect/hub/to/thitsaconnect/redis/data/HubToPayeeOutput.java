package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data;

import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionScenario;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersIDPatchResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersIDPutResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HubToPayeeOutput {

    private String quoteId;

    private String transferId;

    private AmountType amountType;

    private TransactionScenario transactionScenario;

    private boolean isRemittence;

    private String note;

    private QuotesIDPutResponse quotesIDPutResponse;

    private InterledgerData interledgerData;

    private Party payeeParty;

    private Party payerParty;

    private TransfersIDPutResponse transfersIDPutResponse;

    private TransfersIDPatchResponse transfersIDPatchResponse;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterledgerData {

        private String fulfillment;

    }

}
