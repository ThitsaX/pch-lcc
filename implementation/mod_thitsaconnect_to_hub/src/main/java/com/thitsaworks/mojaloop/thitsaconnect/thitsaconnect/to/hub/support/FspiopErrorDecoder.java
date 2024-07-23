package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.component.spring.SpringContext;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import okhttp3.ResponseBody;

import java.io.IOException;

public class FspiopErrorDecoder implements RetrofitRestApi.ErrorDecoder<ErrorInformationResponse> {

    @Override
    public ErrorInformationResponse decode(int status, ResponseBody errorResponseBody) {

        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);

        try {

            return objectMapper.readValue(errorResponseBody.string(), ErrorInformationResponse.class);

        } catch (IOException e) {

            return null;

        }
    }

}
