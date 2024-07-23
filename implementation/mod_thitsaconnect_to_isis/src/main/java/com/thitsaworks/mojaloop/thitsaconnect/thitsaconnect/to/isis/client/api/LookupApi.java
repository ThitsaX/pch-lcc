package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.IsisFspService;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.helper.IsisErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.http.Part;

import java.io.Serializable;
import java.util.Map;

public class LookupApi
    extends
    RetrofitRestApi<IsisFspService, LookupApi.Request, LookupApi.Response, IsisErrorResponse> {

    private final IsisFspService isisFspService;

    private final String endpoint;

    private final String authorization;

    private final PartyIdType partyIdType;

    private final String partyIdentifier;



    public LookupApi(IsisFspService isisFspService, String url,
                     ErrorDecoder<IsisErrorResponse> exceptionDecoder, String authorization,
                     PartyIdType partyIdType, String partyIdentifier) {

        super(exceptionDecoder);

        this.isisFspService = isisFspService;
        this.endpoint = url;
        this.authorization = authorization;
        this.partyIdType=partyIdType;
        this.partyIdentifier=partyIdentifier;
    }

    @Override
    protected Call<Response> call(Request request, Map<String, Object> extra) {

        return this.isisFspService.doLookUp(this.endpoint,this.partyIdType.toString(),this.partyIdentifier,this.authorization );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request implements Serializable {

        @JsonProperty("MobileNumber")
        private String MobileNumber;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {


        @JsonProperty("FATHERLASTNAME")
        private String fatherLastName;

        @JsonProperty("MOTHERLASTNAME")
        private String motherLastName;

        @JsonProperty("FIRSTNAMES")
        private String firstName;

        @JsonProperty("FULLNAME")
        private String fullName;

        @JsonProperty("ACCOUNTID")
        private String accountId;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

        private ErrorResponse errorResponse;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ErrorResponse implements Serializable {
            @JsonProperty("code")
            private String code;

            @JsonProperty("name")
            private String name;

            @JsonProperty("description")
            private String description;

        }

    }

}
