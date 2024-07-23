package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ThitsaConnectCustomException extends Exception {

    public ThitsaConnectCustomException(String message) {

        super(message);
    }

}
