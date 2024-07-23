package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.component.spring.SpringContext;
import okhttp3.ResponseBody;

import java.io.IOException;

public class FspErrorDecoder implements RetrofitRestApi.ErrorDecoder<FspErrorResponse> {

    @Override
    public FspErrorResponse decode(int status, ResponseBody errorResponseBody) {

        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);

        try {

            return objectMapper.readValue(errorResponseBody.string(), FspErrorResponse.class);

        } catch (IOException e) {

            return null;

        }

    }

}
