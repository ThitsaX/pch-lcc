package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api;

import lombok.Value;

@Value
public class FspiopApiSettings {

    private String getPartiesByIdUrl;

    private String getPartiesByIdAndSubIdUrl;

    private String postQuotesUrl;

    private String postTransfersUrl;

}
