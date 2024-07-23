package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub;

import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitService;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.GetPartiesById;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PostQuotes;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PostTransfers;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.exception.FspiopApiException;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.services.FspiopService;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.support.FspiopErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.ConnectException;

@Component
public class ThitsaconnectToHubClient {

    private static final Logger LOG = LoggerFactory.getLogger(ThitsaconnectToHubClient.class);

    @Autowired
    private ThitsaconnectToHubConfiguration.Settings settings;

    private final FspiopService fspiopService;

    public ThitsaconnectToHubClient() {

        this.fspiopService =
                new RetrofitService<>(FspiopService.class, "http://vnextpre.thitsa.io:4000", null, true).getService();

    }

    public GetPartiesById.Response getParties(String sourceId, String destinationId,
                                              String fsiopEncryption,
                                              //String fspiopSignature,
                                              String fspiopUri,String fspiopHttpMethod,
                                              String partyId,
                                              PartyIdType partyIdType, String subPartyId, Currency currency)
            throws FspiopApiException, ConnectException {

        String endpoint = this.settings.getFspiopBaseUrl() + "/parties/" + partyIdType + "/" + partyId;

        if (subPartyId != null && (!subPartyId.isEmpty() || !subPartyId.isBlank())) {
            endpoint = endpoint + "/" + subPartyId;
        }

        GetPartiesById getPartiesById =
                new GetPartiesById(this.fspiopService, endpoint, sourceId, destinationId,
                        fsiopEncryption,fspiopUri,fspiopHttpMethod,
                        currency,
                        new FspiopErrorDecoder());

        GetPartiesById.Response response;

        try {

            response = new GetPartiesById.Response(
                    Integer.toString(getPartiesById.invoke(new GetPartiesById.Request()).code()));

        } catch (RetrofitRestApi.RestException e) {

            if (e.getErrorResponse() != null && e.getErrorResponse() instanceof ErrorInformationResponse) {

                throw new FspiopApiException((ErrorInformationResponse) e.getErrorResponse());

            } else if (e.getCause() instanceof ConnectException) {

                throw new ConnectException(e.getMessage());
            } else {

                throw new FspiopApiException(null);

            }
        }

        return response;

    }

    public PostQuotes.Response postQuote(String fspiopSource, String fspiopDestination,
                                         String fsiopEncryption,
                                        String fspiopDate,
                                         String fspiopSignature,String uri,String httpMethod,
                                         QuotesPostRequest request)
            throws FspiopApiException, ConnectException {


        PostQuotes postQuotes =
                new PostQuotes(this.fspiopService, this.settings.getFspiopBaseUrl() + "/quotes", fspiopSource,
                        fspiopDestination,fspiopDate,
                        fspiopSignature, uri,httpMethod,new FspiopErrorDecoder(), fsiopEncryption);

        PostQuotes.Response response;

        try {

            response = new PostQuotes.Response(Integer.toString(postQuotes.invoke(request).code()));

        } catch (RetrofitRestApi.RestException e) {

            if (e.getErrorResponse() != null && e.getErrorResponse() instanceof ErrorInformationResponse) {

                throw new FspiopApiException((ErrorInformationResponse) e.getErrorResponse());

            } else if (e.getCause() instanceof ConnectException) {

                throw new ConnectException(e.getMessage());
            } else {

                throw new FspiopApiException(null);

            }
        }
        return response;
    }

    public PostTransfers.Response postTransfers(String fspiopSource, String fspiopDestination,
                                                String fsiopEncryption,
                                                String fspiopDate,
                                                String fspiopSignature,String fspiopUri,String fspiopHttpMethod,
                                                TransfersPostRequest request)
            throws FspiopApiException, ConnectException {

        PostTransfers postTransfers =
                new PostTransfers(this.fspiopService, this.settings.getFspiopBaseUrl() + "/transfers", fspiopSource,
                        fspiopDestination,
                        fspiopDate,
                        new FspiopErrorDecoder(),
                        fspiopSignature, fsiopEncryption, fspiopUri, fspiopHttpMethod);

        PostTransfers.Response response;

        try {

            response = new PostTransfers.Response(Integer.toString(postTransfers.invoke(request).code()));

        } catch (RetrofitRestApi.RestException e) {

            if (e.getErrorResponse() != null && e.getErrorResponse() instanceof ErrorInformationResponse) {

                throw new FspiopApiException((ErrorInformationResponse) e.getErrorResponse());

            } else if (e.getCause() instanceof ConnectException) {

                throw new ConnectException(e.getMessage());
            } else {

                throw new FspiopApiException(null);

            }
        }
        return response;
    }

}
