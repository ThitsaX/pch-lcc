package com.thitsaworks.mojaloop.thitsaconnect.component.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor

public abstract class IgnorableException extends ThitsaConnectException {

    protected IgnorableException(String[] params) {

        super(params);
    }

}
