package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.helper;

import com.thitsaworks.mojaloop.thitsaconnect.component.mojaloop.ErrorCode;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitRestApi;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Extension;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.api.LookupApi;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("customErrorProcessor")
public class CustomErrorProcessor {

    public ErrorInformation process(Exception exception) throws JSONException {


        String statusCode = "5000";
        String errorMessage = "Downstream API failed.";
        String description = "Unknown";


        if (exception instanceof RetrofitRestApi.RestException) {
            RetrofitRestApi.RestException restException = (RetrofitRestApi.RestException) exception;
            Object errorResponse = restException.getErrorResponse();

            if (errorResponse != null) {
                statusCode = getFieldFromErrorResponse(errorResponse, "code");
                errorMessage = getFieldFromErrorResponse(errorResponse, "name");
                description = getFieldFromErrorResponse(errorResponse, "description");
            }
        }


        ExtensionList extensionList = new ExtensionList();
        Extension extension = new Extension();

        List<Extension> extensionListItems = new ArrayList<>();
        extension.setKey("description");
        if (description.isEmpty()) {
            description = errorMessage;
        }
        extension.setValue(description);
        extensionListItems.add(extension);

        extensionList.setExtension(extensionListItems);

        ErrorInformation errorInformation = new ErrorInformation();
        errorInformation.setErrorCode(statusCode);
        errorInformation.setErrorDescription(errorMessage);
        errorInformation.setExtensionList(extensionList);

        return errorInformation;

    }

    private String getFieldFromErrorResponse(Object errorResponse, String fieldName) {

        try {
            java.lang.reflect.Field field = errorResponse.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (String) field.get(errorResponse);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Handle the exception as needed
            return "Unknown";
        }
    }

}