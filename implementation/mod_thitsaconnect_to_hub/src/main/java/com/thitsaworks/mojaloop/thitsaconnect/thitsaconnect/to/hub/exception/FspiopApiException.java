package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.exception;

import com.thitsaworks.mojaloop.thitsaconnect.component.exception.ThitsaConnectException;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ErrorInformationResponse;
import lombok.Getter;

@Getter
public class FspiopApiException extends ThitsaConnectException {

    private static final long serialVersionUID = 1L;

    private final ErrorInformationResponse errorResponse;

    public FspiopApiException(ErrorInformationResponse errorResponse) {

        this.errorResponse = errorResponse;

    }

    @Override
    public String errorCode() {

        return this.errorResponse.getErrorInformation().getErrorCode();
    }

    @Override
    public String defaultErrorMessage() {

        if (this.errorResponse.getErrorInformation() != null) {

            return this.errorResponse.getErrorInformation().getErrorDescription();

        }

        return this.errorResponse.toString();
    }

    @Override
    public boolean requireTranslation() {

        return false;
    }

}
