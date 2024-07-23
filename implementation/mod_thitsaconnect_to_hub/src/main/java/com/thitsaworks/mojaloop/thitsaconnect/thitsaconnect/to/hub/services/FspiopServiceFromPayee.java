package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.services;

import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutPartiesByIdandSubId;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutPartiesError;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutQuotes;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutQuotesError;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutTransfers;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutTransfersError;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Url;

public interface FspiopServiceFromPayee {

    @PUT
    Call<PutPartiesByIdandSubId.Response> putParties(@Url String endpoint,
                                                     @Header("fspiop-source") String fspiopSource,
                                                     @Header("fspiop-destination") String fspiopDestination,
                                                     @Header("date") String fspiopDate, @Header("accept") String accept,
                                                     @Header("content-type") String contentType,
                                                     @Body String request);

    @PUT
    Call<PutPartiesError.Response> putPartiesError(@Url String endpoint,
                                                   @Header("fspiop-source") String fspiopSource,
                                                   @Header("fspiop-destination") String fspiopDestination,
                                                   @Header("date") String fspiopDate, @Header("accept") String accept,
                                                   @Header("content-type") String contentType,
                                                   @Body String request);

    @PUT
    Call<PutQuotes.Response> putQuotes(@Url String endpoint,
                                       @Header("fspiop-source") String fspiopSource,
                                       @Header("fspiop-destination") String fspiopDestination,
                                       @Header("date") String fspiopDate,
                                       @Header("fspiop-encryption") String fspiopEncryption,
                                       @Header("fspiop-signature") String fsiopSignature,
                                       @Header("fspiop-uri") String fspiopUri,
                                       @Header("fspiop-http-method") String fspiopHttpMethod,
                                       @Header("accept") String accept,
                                       @Header("content-type") String contentType, @Body String request);

    @PUT
    Call<PutQuotesError.Response> putQuotesError(@Url String endpoint,
                                                 @Header("fspiop-source") String fspiopSource,
                                                 @Header("fspiop-destination") String fspiopDestination,
                                                 @Header("date") String fspiopDate, @Header("accept") String accept,
                                                 @Header("content-type") String contentType, @Body String request);

    @PUT
    Call<PutTransfers.Response> putTransfers(@Url String endpoint,
                                             @Header("fspiop-source") String fspiopSource,
                                             @Header("fspiop-destination") String fspiopDestination,
                                             @Header("date") String fspiopDate,
                                             @Header("fspiop-encryption") String fspiopEncryption,
                                             @Header("fspiop-signature") String fsiopSignature,
                                             @Header("fspiop-uri") String fspiopUri,
                                             @Header("fspiop-http-method") String fspiopHttpMethod,
                                             @Header("accept") String accept,
                                             @Header("content-type") String contentType, @Body String request);

    @PUT
    Call<PutTransfersError.Response> putTransfersError(@Url String endpoint,
                                                       @Header("fspiop-source") String fspiopSource,
                                                       @Header("fspiop-destination") String fspiopDestination,
                                                       @Header("date") String fspiopDate,
                                                       @Header("accept") String accept,
                                                       @Header("content-type") String contentType,
                                                       @Body String request);

    void postTransfers(@Url String endpoint, @Body TransfersPostRequest request);

}
