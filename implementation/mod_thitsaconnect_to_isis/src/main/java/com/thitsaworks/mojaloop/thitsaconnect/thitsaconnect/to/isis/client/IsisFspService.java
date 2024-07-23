package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client;

import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.api.LookupApi;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.api.QuoteApi;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.api.TransferApi;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IsisFspService {

    @GET
    Call<LookupApi.Response> doLookUp(@Url String endpoint,
                                      @Query("partyIdType") String partyIdType,
                                      @Query("partyIdentifier") String partyIdentifier,
                                      @Header("Authorization") String authorization
                                     );

    @POST
    Call<QuoteApi.Response> doQuote(@Url String endpoint,
                                    @Body QuoteApi.Request request,
                                    @Header("Authorization") String authorization
                                   );

    @POST
    Call<TransferApi.Response> doTransfer(@Url String endpoint,
                                          @Body TransferApi.Request request,
                                          @Header("Authorization") String authorization
                                         );

}
