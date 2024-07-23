package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub;

import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitService;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartiesTypeIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutPartiesByIdandSubId;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutPartiesError;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutQuotes;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutQuotesError;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutTransfers;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutTransfersError;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.exception.FspiopApiException;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.services.FspiopServiceFromPayee;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.support.FspiopErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.ConnectException;

@Component
public class ThitsaconnectToHubFromPayeeClient {

    private static final Logger LOG = LoggerFactory.getLogger(ThitsaconnectToHubFromPayeeClient.class);

    @Autowired
    private ThitsaconnectToHubConfiguration.Settings settings;

    private final FspiopServiceFromPayee fspiopServiceFromPayee;

    public ThitsaconnectToHubFromPayeeClient() {

        this.fspiopServiceFromPayee =
                new RetrofitService<>(FspiopServiceFromPayee.class, "http://vnextpre.thitsa.io:4000", null,
                        true).getService();

    }

    public PutPartiesByIdandSubId.Response putParties(String sourceId, String destinationId, Currency currency,
                                                      String date, PartiesTypeIDPutResponse request, String accept,
                                                      String contentType) throws FspiopApiException, ConnectException {

        String endpoint =
                this.settings.getFspiopBaseUrl() + "/parties/" + request.getParty().getPartyIdInfo().getPartyIdType() +
                        "/" + request.getParty().getPartyIdInfo().getPartyIdentifier();

        if (request.getParty().getPartyIdInfo().getPartySubIdOrType() != null &&
                !request.getParty().getPartyIdInfo().getPartySubIdOrType().isEmpty()) {

            endpoint = endpoint + "/" + request.getParty().getPartyIdInfo().getPartySubIdOrType();
        }

        PutPartiesByIdandSubId putPartiesByIdandSubId =
                new PutPartiesByIdandSubId(this.fspiopServiceFromPayee, endpoint, sourceId, destinationId, currency,
                        date, accept, contentType, new FspiopErrorDecoder());

        PutPartiesByIdandSubId.Response response;

        try {
            response = putPartiesByIdandSubId.invoke(request).body();

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

    public PutPartiesError.Response putPartiesError(String sourceId, String destinationId, Currency currency,
                                                    String date, ErrorInformationResponse request, PartyIdType idType,
                                                    String idValue, String idSubValue, String accept,
                                                    String contentType) throws FspiopApiException, ConnectException {

        String endpoint = this.settings.getFspiopBaseUrl() + "/parties/" + idType + "/" + idValue;

        if (idSubValue != null) {
            endpoint = endpoint + "/" + idSubValue;
        }
        endpoint = endpoint + "/error";

        PutPartiesError putPartiesError =

                new PutPartiesError(this.fspiopServiceFromPayee, endpoint, sourceId, destinationId, currency, date,
                        accept, contentType, new FspiopErrorDecoder());

        PutPartiesError.Response response;

        try {

            response = putPartiesError.invoke(request).body();

        } catch (RetrofitRestApi.RestException e) {

            LOG.error("error :" + e.getCause());

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

    public PutQuotes.Response putQuote(String quoteId, String fspiopSource, String fspiopDestination, String fspiopDate,
                                       String fspiopEncryption,  String fspiopSignature,
                                       String fspiopUri,String fspiopHttpMethod,
                                       String accept, String contentType, QuotesIDPutResponse request)
            throws FspiopApiException, ConnectException {

        String endpoint = this.settings.getFspiopBaseUrl() + "/quotes/" + quoteId;

        PutQuotes putQuotes =
                new PutQuotes(this.fspiopServiceFromPayee, endpoint, fspiopSource, fspiopDestination, fspiopDate,
                        fspiopEncryption, fspiopSignature,fspiopUri,fspiopHttpMethod,
                        accept, contentType, new FspiopErrorDecoder());

        PutQuotes.Response response;

        try {

            response = putQuotes.invoke(request).body();

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

    public PutTransfers.Response putTransfers(String transferId, String fspiopSource, String fspiopDestination,
                                              String fspiopDate,
                                              String fspiopEncryption,  String fspiopSignature,
                                              String fspiopUri,String fspiopHttpMethod,
                                              String accept, String contentType,
                                              TransfersIDPutResponse request)
            throws FspiopApiException, ConnectException {

        String endpoint = this.settings.getFspiopBaseUrl() + "/transfers/" + transferId;

        PutTransfers putTransfers =
                new PutTransfers(this.fspiopServiceFromPayee, endpoint, transferId, fspiopSource, fspiopDestination,
                        fspiopDate,
                        accept, contentType, new FspiopErrorDecoder(), fspiopEncryption, fspiopUri, fspiopHttpMethod, fspiopSignature);

        PutTransfers.Response response;

        try {

            response = putTransfers.invoke(request).body();

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

    public PutQuotesError.Response putQuotesError(String quoteId, String fspiopSource, String fspiopDestination,
                                                  String fspiopDate, String accept, String contentType,
                                                  ErrorInformationResponse request)
            throws FspiopApiException, ConnectException {

        String endpoint = this.settings.getFspiopBaseUrl() + "/quotes/" + quoteId + "/error";

        PutQuotesError putQuotesError =
                new PutQuotesError(this.fspiopServiceFromPayee, endpoint, fspiopSource, fspiopDestination, fspiopDate,
                        accept, contentType, new FspiopErrorDecoder());

        PutQuotesError.Response response;

        try {

            response = putQuotesError.invoke(request).body();

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

    public PutTransfersError.Response putTransfersError(String transferId, String fspiopSource,
                                                        String fspiopDestination, String fspiopDate, String accept,
                                                        String contentType, ErrorInformationResponse request)
            throws FspiopApiException, ConnectException {

        String endpoint = this.settings.getFspiopBaseUrl() + "/transfers/" + transferId + "/error";

        PutTransfersError putTransfersError =
                new PutTransfersError(this.fspiopServiceFromPayee, endpoint, transferId, fspiopSource,
                        fspiopDestination, fspiopDate, accept, contentType, new FspiopErrorDecoder());

        PutTransfersError.Response response;

        try {

            response = putTransfersError.invoke(request).body();

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
