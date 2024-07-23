package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Extension;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.GeoCode;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartiesTypeIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersIDPutResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HubToPayerOutput {

    private PartiesRequest partiesRequest;

    private PartiesResponse partiesResponse;

    private PostQuoteInput postQuoteInput;

    private PostQuoteOutput postQuoteOutput;

    private PostTransfersOutput postTransfersOutput;

    private ErrorOutput errorOutput;

    private RedisPartyData payeeParty;

    private RedisPartyData payerParty;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisPartyData {

        private String type;

        private Party party;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartiesRequest {

        private String homeTransactionId;

        private AmountType amountType;

        private Currency currency;

        private com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionType transactionType;

        private String note;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartiesResponse {

        private PartiesTypeIDPutResponse partiesTypeIDPutResponse;

        private String transferId;

        private StateEnum currentState;

        private String initiatedTimestamp;

        private String direction;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostQuoteInput {

        private String quoteId;

        private String transactionId;

        private FspParty to;

        private FspParty from;

        private AmountType amountType;

        private BigDecimal amount;

        private String currency;

        private BigDecimal feesAmount;

        private String feesCurrency;

        private String transactionType;

        private String initiator;

        private String initiatorType;

        private GeoCode geoCode;

        private String note;

        private String expiration;

        private List<Extension> extensionList;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostQuoteOutput {

        private QuotesIDPutResponse quotesIDPutResponse;

        private StateEnum currentState;

        private String initiatedTimestamp;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostTransfersInput {

        private String transferId;

        private Quote quote;

        private FspParty from;

        private FspParty to;

        private String amountType;

        private String currency;

        private BigDecimal amount;

        private TransactionType transactionType;

        private String note;

        private List<Extension> extensionList;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostTransfersOutput {

        private TransfersIDPutResponse transfersIDPutResponse;

        private StateEnum currentState;

        private String initiatedTimestamp;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FspParty {

        private String type;

        private PartyIdType idType;

        private String idValue;

        private String idSubValue;

        private String displayName;

        private String firstName;

        private String middleName;

        private String lastName;

        private String dateOfBirth;

        private String merchantClassificationCode;

        private String fspId;

        private List<Extension> extensionList;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Quote {

        private String quoteId;

        private String transactionId;

        private BigDecimal transferAmount;

        private String transferAmountCurrency;

        private BigDecimal payeeReceiveAmount;

        private String payeeReceiveAmountCurrency;

        private BigDecimal payeeFspFeeAmount;

        private String payeeFspFeeAmountCurrency;

        private BigDecimal payeeFspCommissionAmount;

        private String payeeFspCommissionAmountCurrency;

        private String expiration;

        private GeoCode geoCode;

        private List<Extension> extensionList;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransactionType {

        private String scenario;

        private String initiator;

        private String initiatorType;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorOutput {

        private ErrorInformationResponse errorInformationResponse;

    }

}
