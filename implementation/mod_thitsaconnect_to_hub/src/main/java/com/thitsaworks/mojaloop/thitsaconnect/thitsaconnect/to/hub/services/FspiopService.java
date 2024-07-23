package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.services;

import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.GetPartiesById;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PostQuotes;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PostTransfers;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface FspiopService {

    @GET
    Call<GetPartiesById.Response> getPartiesById(@Url String endPoint,
                                                 @Header("fspiop-source") String fspiopSource,
                                                 @Header("fspiop-destination") String fspiopDestination,
                                                 @Header("date") String fspiopDate, @Header("accept") String accept,
                                                 @Header("content-type") String contentType,
                                                 @Query("currency") String currency);

    @POST
    Call<PostQuotes.Response> postQuotes(@Url String endpoint,
                                         @Header("fspiop-source") String fspiopSource,
                                         @Header("fspiop-destination") String fspiopDestination,
                                         @Header("date") String fspiopDate,
                                         @Header("fspiop-signature") String fsiopSignature,
                                         @Header("fspiop-uri") String fspiopUri,
                                         @Header("fspiop-http-method") String fspiopHttpMethod,
                                         @Header("fspiop-encryption") String fspiopEncryption,
                                         @Header("accept") String accept,
                                         @Header("content-type") String contentType, @Body String request);

    @POST
    Call<PostTransfers.Response> postTransfers(@Url String endpoint,
                                               @Header("fspiop-source") String fspiopSource,
                                               @Header("fspiop-destination") String fspiopDestination,
                                               @Header("date") String fspiopDate,
                                               @Header("fspiop-signature") String fsiopSignature,
                                               @Header("fspiop-uri") String fspiopUri,
                                               @Header("fspiop-http-method") String fspiopHttpMethod,
                                               @Header("fspiop-encryption") String fspiopEncryption,
                                               @Header("accept") String accept,
                                               @Header("content-type") String contentType, @Body String request);

}
