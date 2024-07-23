package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.services.FspiopServiceFromPayee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import retrofit2.Call;

import java.io.Serializable;
import java.util.Map;

public class PutTransfersError
        extends
        RetrofitRestApi<FspiopServiceFromPayee, ErrorInformationResponse, PutTransfersError.Response, ErrorInformationResponse> {

    private final FspiopServiceFromPayee fspiopiopServiceForPayee;

    private final String endpoint;

    private final String transferId;

    private final String fspiopSource;

    private final String fspiopDestination;

    //headers
    private final String fspiopDate;

    private final String accept;

    private final String contentType;

    public PutTransfersError(FspiopServiceFromPayee fspiopiopServiceForPayee, String endpoint, String transferId,
                             String fspiopSource, String fspiopDestination, String fspiopDate,
                             String accept, String contentType,
                             RetrofitRestApi.ErrorDecoder<ErrorInformationResponse> exceptionDecoder) {

        super(exceptionDecoder);

        this.fspiopiopServiceForPayee = fspiopiopServiceForPayee;
        this.endpoint = endpoint;
        this.transferId = transferId;
        this.fspiopSource = fspiopSource;
        this.fspiopDestination = fspiopDestination;
        this.fspiopDate = fspiopDate;
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
    protected Call<Response> call(ErrorInformationResponse request, Map<String, Object> extra)
            throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        String cleanRequest = mapper.writeValueAsString(request);

        return this.fspiopiopServiceForPayee.putTransfersError(this.endpoint, this.fspiopSource, this.fspiopDestination,
                this.fspiopDate, this.accept, this.contentType, cleanRequest);

    }

}
