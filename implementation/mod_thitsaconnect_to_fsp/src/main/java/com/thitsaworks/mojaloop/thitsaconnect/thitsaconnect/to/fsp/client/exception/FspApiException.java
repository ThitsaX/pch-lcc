package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.exception;

import com.thitsaworks.mojaloop.thitsaconnect.component.exception.ThitsaConnectException;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.helper.FspErrorResponse;
import lombok.Getter;

@Getter

public class FspApiException extends ThitsaConnectException {

    private static final long serialVersionUID = 1L;

    private final FspErrorResponse errorResponse;

    public FspApiException(FspErrorResponse errorResponse) {

        this.errorResponse = errorResponse;

    }

    @Override
    public String errorCode() {

        return "FSP_API_EXCEPTION";
    }

    @Override
    public String defaultErrorMessage() {

        if (this.errorResponse.getErrors() != null && this.errorResponse.getErrors().length >= 1) {

            return this.errorResponse.getErrors()[0].getDefaultUserMessage();

        }

        return this.errorResponse.getDefaultUserMessage();
    }

    @Override
    public boolean requireTranslation() {

        return false;
    }

}
