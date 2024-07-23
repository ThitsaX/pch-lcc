package com.thitsaworks.mojaloop.thitsaconnect.component.exception;

import java.text.MessageFormat;

public class SystemProblemException extends ThitsaConnectException {

    public SystemProblemException() {

        super();
    }

    public SystemProblemException(String[] params) {

        super(params);
    }

    @Override
    public String errorCode() {

        return "SYSTEM_PROBLEM";
    }

    @Override
    public String defaultErrorMessage() {

        return MessageFormat.format("Something went wrong. Error : {0}", this.params[0]);
    }

    @Override
    public boolean requireTranslation() {

        return false;
    }

}
