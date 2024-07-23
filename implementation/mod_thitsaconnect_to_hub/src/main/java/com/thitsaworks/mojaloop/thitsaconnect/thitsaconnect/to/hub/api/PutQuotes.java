package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.services.FspiopServiceFromPayee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import retrofit2.Call;

import java.io.Serializable;
import java.util.Map;

public class PutQuotes
        extends
        RetrofitRestApi<FspiopServiceFromPayee, QuotesIDPutResponse, PutQuotes.Response, ErrorInformationResponse> {

    private final FspiopServiceFromPayee fspiopiopServiceForPayee;

    private final String endpoint;

    private final String fspiopSource;

    private final String fspiopDestination;

    private final String fspiopEncryption;

    private final String fspiopUri;

    private final String fspiopHttpMethod;

    private final String fspiopSignature;
    //headers
    private final String fspiopDate;

    private final String accept;

    private final String contentType;

    public PutQuotes(FspiopServiceFromPayee fspiopiopServiceForPayee, String endpoint, String fspiopSource,
                     String fspiopDestination, String fspiopDate,
                     String fspiopEncryption,  String fspiopSignature,
                     String fspiopUri, String fspiopHttpMethod,
                     String accept, String contentType,
                     ErrorDecoder<ErrorInformationResponse> exceptionDecoder) {

        super(exceptionDecoder);

        this.fspiopiopServiceForPayee = fspiopiopServiceForPayee;
        this.endpoint = endpoint;
        this.fspiopSource = fspiopSource;
        this.fspiopDestination = fspiopDestination;
        this.fspiopDate = fspiopDate;
        this.fspiopEncryption = fspiopEncryption;
        this.fspiopSignature = fspiopSignature;
        this.fspiopUri = fspiopUri;
        this.fspiopHttpMethod = fspiopHttpMethod;
        this.accept = accept;
        this.contentType = contentType;


    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Response implements Serializable {

        @JsonProperty("status")
        String status;

    }

    @Override
    protected Call<Response> call(QuotesIDPutResponse request, Map<String, Object> extra)
            throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        String cleanRequest = mapper.writeValueAsString(request);

        return this.fspiopiopServiceForPayee.putQuotes(this.endpoint, this.fspiopSource, this.fspiopDestination, this.fspiopDate,
                this.fspiopEncryption,   this.fspiopSignature,
                this.fspiopUri,this.fspiopHttpMethod,
                this.accept, this.contentType, cleanRequest);

    }

}
