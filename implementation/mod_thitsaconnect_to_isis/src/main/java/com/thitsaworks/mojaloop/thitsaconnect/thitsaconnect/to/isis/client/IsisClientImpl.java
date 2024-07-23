package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client;

import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Extension;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.FspClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.ConfirmationForTransfer;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.Lookup;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.Quote;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.ReservationForTransfer;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.ThitsaconnectToIsisConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.api.LookupApi;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.api.QuoteApi;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.api.TransferApi;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.helper.CustomErrorProcessor;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.helper.IsisErrorDecoder;
import okhttp3.Credentials;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Qualifier("isisFspClientImpl")
public class IsisClientImpl implements FspClient {

    private static final Logger LOG = LoggerFactory.getLogger(IsisClientImpl.class);

    @Autowired
    private IsisFspService isisFspService;

    @Autowired
    private ThitsaconnectToIsisConfiguration.Settings isisConfigurationSetting;

    @Autowired
    private CustomErrorProcessor customErrorProcessor;

    private String getCredentials() {

        String credentials = Credentials.basic(this.isisConfigurationSetting.getUsername(),
                                               this.isisConfigurationSetting.getPassword());
        return credentials;
    }

    @Override
    public Lookup.Response doLookUp(Lookup.Request request) throws JSONException {

        Lookup.Response response = new Lookup.Response();
        LookupApi.Request apiRequest = new LookupApi.Request();

        try {

            LookupApi lookupApi = new LookupApi(this.isisFspService,
                                                this.isisConfigurationSetting.getIsIsUrl() +
                                                    "retrieve_info", new IsisErrorDecoder(), getCredentials(),
                                                request.getIdType(),
                                                request.getIdValue());

            var apiResponse = lookupApi.invoke(apiRequest).body();

            if (apiResponse != null) {
                response.setType("BUSINESS");
                response.setIdType(request.getIdType());
                response.setIdValue(request.getIdValue());
                response.setDisplayName(apiResponse.getFullName());
                response.setFirstName(apiResponse.getFirstName());
                response.setMiddleName(apiResponse.getMotherLastName());
                response.setLastName(apiResponse.getFatherLastName());
                response.setDisplayName(apiResponse.getFullName());
                ExtensionList extensionList = apiResponse.getExtensionList();
                Extension accountId = new Extension();
                accountId.setKey("AccountId");
                accountId.setValue(apiResponse.getAccountId());
                extensionList.addExtensionItem(accountId);
                response.setExtensionList(extensionList);

            }

        } catch (RetrofitRestApi.RestException e) {

            LOG.error("ERROR: " + e);

            response.setError(this.customErrorProcessor.process(e));
        }

        return response;
    }

    @Override
    public Quote.Response doQuote(Quote.Request request) {

        Quote.Response response = new Quote.Response();
        QuoteApi.Request apiRequest = new QuoteApi.Request();

        try {

            apiRequest.setQuoteID(request.getQuoteId());
            Money transferAmount = new Money();
            transferAmount.setAmount(String.valueOf(request.getAmount()));
            transferAmount.setCurrency(request.getCurrency());
            apiRequest.setTransferAmount(transferAmount);

            QuoteApi quoteApi = new QuoteApi(this.isisFspService,
                                             this.isisConfigurationSetting.getIsIsUrl() +
                                                 "isis_quote", new IsisErrorDecoder(), getCredentials());

            var apiResponse = quoteApi.invoke(apiRequest).body();
            if (apiResponse != null) {

                BigDecimal payeeFeeAndCommission = new BigDecimal(apiResponse.getPayeeFspCommission().getAmount()).add(
                    new BigDecimal(apiResponse.getPayeeFspFee().getAmount()));

                response.setQuoteId(request.getQuoteId());
                response.setTransactionId(request.getTransactionId());
                response.setTransferAmount(new BigDecimal(request.getAmount().stripTrailingZeros().toPlainString()));
                response.setTransferAmountCurrency(request.getCurrency().toString());
                BigDecimal payeeReceiveAmount = request.getAmount().subtract(payeeFeeAndCommission);
                response.setPayeeReceiveAmount(new BigDecimal(payeeReceiveAmount.stripTrailingZeros().toPlainString()));
                response.setPayeeReceiveAmountCurrency(request.getCurrency().toString());
                response.setPayeeFspFeeAmount(new BigDecimal(apiResponse.getPayeeFspFee().getAmount().stripTrailing()));
                response.setPayeeFspFeeAmountCurrency(request.getCurrency().toString());
                response.setPayeeFspCommissionAmount(new BigDecimal(apiResponse.getPayeeFspCommission().getAmount()));
                response.setPayeeFspCommissionAmountCurrency(request.getCurrency().toString());
                response.setExpiration(request.getExpiration());
                response.setGeoCode(request.getGeoCode());
                response.setExtensionList(request.getExtensionList());

            }

        } catch (RetrofitRestApi.RestException e) {

            LOG.error("ERROR: " + e);

        }

        return response;
    }

    @Override
    public ReservationForTransfer.Response doReservationForTransfer(ReservationForTransfer.Request request) {

        ReservationForTransfer.Response response = new ReservationForTransfer.Response();

        return response;
    }

    @Override
    public ConfirmationForTransfer.Response doConfirmationForTransfer(ConfirmationForTransfer.Request request)
        throws RetrofitRestApi.RestException {

        ConfirmationForTransfer.Response response = new ConfirmationForTransfer.Response();

        TransferApi transferApi = new TransferApi(this.isisFspService,
                                                  this.isisConfigurationSetting.getIsIsUrl() +
                                                      "isis_transfer", new IsisErrorDecoder(), getCredentials());

        TransferApi.Request apiRequest = new TransferApi.Request();
        apiRequest.setTransferID(request.getTransferId());
        apiRequest.setTransferAmount(request.getQuotesIDPutResponse().getTransferAmount());

        String desiredKeyRateInfo = "AccountId";
        String accountId = "";
        List<Extension> keyValueList = request.getPayeeParty().getPartyIdInfo().getExtensionList().getExtension();

        for (Extension keyValue : keyValueList) {
            String key = keyValue.getKey();
            String value = keyValue.getValue();

            if (desiredKeyRateInfo.equals(key)) {
                accountId = value;
                break;
            }
        }

        apiRequest.setAccountID(accountId);
        apiRequest.setFromFspId(request.getPayerParty().getPartyIdInfo().getFspId());
        apiRequest.setToFspId(request.getPayeeParty().getPartyIdInfo().getFspId());
        apiRequest.setRemittence(request.isRemittence());
        apiRequest.setNote(request.getNote() != null ? request.getNote() : "");


        var apiResponse = transferApi.invoke(apiRequest).body();

        if (apiResponse != null) {
            response.setHomeTransactionId(apiResponse.getNoteCreditID());

        }

        return response;
    }

}
