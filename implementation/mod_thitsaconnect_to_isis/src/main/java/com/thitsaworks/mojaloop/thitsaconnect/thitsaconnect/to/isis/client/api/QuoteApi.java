package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.IsisFspService;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.helper.IsisErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import retrofit2.Call;

import java.io.Serializable;
import java.util.Map;

public class QuoteApi
    extends
    RetrofitRestApi<IsisFspService, QuoteApi.Request, QuoteApi.Response, IsisErrorResponse> {

    private final IsisFspService isisFspService;

    private final String endpoint;

    private final String authorization;

    public QuoteApi(IsisFspService isisFspService, String url,
                    ErrorDecoder<IsisErrorResponse> exceptionDecoder, String authorization
                   ) {

        super(exceptionDecoder);

        this.isisFspService = isisFspService;
        this.endpoint = url;
        this.authorization = authorization;

    }

    @Override
    protected Call<Response> call(Request request, Map<String, Object> extra) {

        return this.isisFspService.doQuote(this.endpoint, request, this.authorization);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request implements Serializable {

        @JsonProperty("quoteID")
        private String quoteID;

        private Money transferAmount;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {

        @JsonProperty("quoteID")
        private String quoteID;

        private Money payeeFspFee;

        private Money payeeFspCommission;

    }

}
