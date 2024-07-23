package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.services.FspiopService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import retrofit2.Call;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class GetPartiesById
        extends
        RetrofitRestApi<FspiopService, GetPartiesById.Request, GetPartiesById.Response, ErrorInformationResponse> {

    private final FspiopService fspiopiopService;

    private final String endPoint;

    private final String fspiopSource;

    private final String fspiopDestination;

    private final String fspiopEncryption;


    //private final String fsiopSignature;

    private final String fspiopuri;

    private final String fspiopHttpMethod;

    private final Currency currency;

    //headers
    private final String fspiopDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

    private final String accept = "application/vnd.interoperability.parties+json;version=1.1";

    private final String contentType = "application/vnd.interoperability.parties+json;version=1.1";

    public GetPartiesById(FspiopService fspiopService, String endPoint, String fspiopSource, String fspiopDestination,
                          // String fsiopSignature,
              String fspiopEncryption,String fspiopuri, String fspiopHttpMethod,
                          Currency currency, ErrorDecoder<ErrorInformationResponse> exceptionDecoder) {

        super(exceptionDecoder);

        this.fspiopiopService = fspiopService;
        this.endPoint = endPoint;
        this.fspiopSource = fspiopSource;
        this.fspiopDestination = fspiopDestination;
        this.currency = currency;
      //  this.fsiopSignature = fsiopSignature;
        this.fspiopEncryption=fspiopEncryption;
        this.fspiopuri = fspiopuri;
        this.fspiopHttpMethod = fspiopHttpMethod;
    }

    @Getter
    @NoArgsConstructor
    public static class Request implements Serializable {

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {

        @JsonProperty("status")
        private String status;

    }

    @Override
    protected Call<GetPartiesById.Response> call(GetPartiesById.Request request, Map<String, Object> extra) {

        return this.fspiopiopService.getPartiesById(this.endPoint, this.fspiopSource, this.fspiopDestination,
                this.fspiopDate, this.accept, this.contentType, this.currency.toString());

    }

}
