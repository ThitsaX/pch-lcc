package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.component.spring.SpringContext;
import okhttp3.ResponseBody;

import java.io.IOException;

public class IsisErrorDecoder implements RetrofitRestApi.ErrorDecoder<IsisErrorResponse> {

    @Override
    public IsisErrorResponse decode(int status, ResponseBody errorResponseBody) {

        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);

        try {

            return objectMapper.readValue(errorResponseBody.string(), IsisErrorResponse.class);

        } catch (IOException e) {

            return null;

        }

    }

}
