package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartiesTypeIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.services.FspiopServiceFromPayee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import retrofit2.Call;

import java.io.Serializable;
import java.util.Map;

public class PutPartiesByIdandSubId extends
        RetrofitRestApi<FspiopServiceFromPayee, PartiesTypeIDPutResponse, PutPartiesByIdandSubId.Response, ErrorInformationResponse> {

    private final FspiopServiceFromPayee fspiopiopService;

    private final String fspiopDate;

    private final String endPoint;

    private final String fspiopSource;

    private final String fspiopDestination;

    private final Currency currency;

    private final String accept;

    private final String contentType;

    public PutPartiesByIdandSubId(FspiopServiceFromPayee fspiopService, String endPoint, String fspiopSource,
                                  String fspiopDestination, Currency currency, String fspiopDate, String accept,
                                  String contentType,
                                  RetrofitRestApi.ErrorDecoder<ErrorInformationResponse> exceptionDecoder) {

        super(exceptionDecoder);

        this.fspiopiopService = fspiopService;
        this.endPoint = endPoint;
        this.fspiopSource = fspiopSource;
        this.fspiopDestination = fspiopDestination;
        this.currency = currency;
        this.fspiopDate = fspiopDate;
        this.accept = accept;
        this.contentType = contentType;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {

        // 202 accept
        @JsonProperty("status")
        private String status;

    }

    @Override
    protected Call<PutPartiesByIdandSubId.Response> call(PartiesTypeIDPutResponse request, Map<String, Object> extra)
            throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        String cleanRequest = mapper.writeValueAsString(request);

        return this.fspiopiopService.putParties(this.endPoint, this.fspiopSource, this.fspiopDestination,
                this.fspiopDate, this.accept, this.contentType, cleanRequest);
    }

}
