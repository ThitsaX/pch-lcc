package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.services.FspiopService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import retrofit2.Call;

import java.io.Serializable;
import java.util.Map;

public class PostTransfers
        extends RetrofitRestApi<FspiopService, TransfersPostRequest, PostTransfers.Response, ErrorInformationResponse> {

    private final FspiopService fspiopiopService;

    private final String endpoint;

    private final String fspiopSource;

    private final String fspiopDestination;

    //headers
    private final String fspiopDate ;

    private final String fspiopSignature;

    private final String fspiopEncryption;

    private final String fspiopUri;

    private final String fspiopHttpMethod;


    private final String accept = "application/vnd.interoperability.transfers+json;version=1.1";

    private final String contentType = "application/vnd.interoperability.transfers+json;version=1.1";

    public PostTransfers(FspiopService fspiopService, String endpoint, String fspiopSource, String fspiopDestination,
                         String fspiopDate,
                         ErrorDecoder<ErrorInformationResponse> exceptionDecoder,
                         String fspiopSignature, String fspiopEncryption,
                         String fspiopUri, String fspiopHttpMethod) {

        super(exceptionDecoder);

        this.fspiopiopService = fspiopService;
        this.endpoint = endpoint;
        this.fspiopSource = fspiopSource;
        this.fspiopDestination = fspiopDestination;
        this.fspiopDate=fspiopDate;
        this.fspiopSignature = fspiopSignature;
        this.fspiopEncryption = fspiopEncryption;
        this.fspiopUri = fspiopUri;
        this.fspiopHttpMethod = fspiopHttpMethod;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Request implements Serializable {

        @JsonProperty("transferId")
        String transferId;

        @JsonProperty("payeeFsp")
        String payeeFsp;

        @JsonProperty("payerFsp")
        String payerFsp;

        @JsonProperty("amount")
        Money amount;

        @JsonProperty("ilpPacket")
        String ilpPacket;

        @JsonProperty("condition")
        String condition;

        @JsonProperty("expiration")
        String expiration;

        @JsonProperty("extensionList")
        ExtensionList extensionList;

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
    protected Call<Response> call(TransfersPostRequest request, Map<String, Object> extra)
            throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        String cleanRequest = mapper.writeValueAsString(request);

        return this.fspiopiopService.postTransfers(this.endpoint, this.fspiopSource, this.fspiopDestination,
                this.fspiopDate,
                this.fspiopSignature,this.fspiopUri,this.fspiopHttpMethod,this.fspiopEncryption,
                this.accept, this.contentType, cleanRequest);

    }

}
