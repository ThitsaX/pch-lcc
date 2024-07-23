package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client;

import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.ConfirmationForTransfer;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.Lookup;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.Quote;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model.ReservationForTransfer;
import org.json.JSONException;
import org.springframework.stereotype.Component;

@Component
public interface FspClient {

    Lookup.Response doLookUp(Lookup.Request request) throws JSONException;

    Quote.Response doQuote(Quote.Request request);

    ReservationForTransfer.Response doReservationForTransfer(ReservationForTransfer.Request request);

    ConfirmationForTransfer.Response doConfirmationForTransfer(ConfirmationForTransfer.Request request)
        throws RetrofitRestApi.RestException;

}
