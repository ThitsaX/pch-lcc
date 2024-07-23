package com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JwtTokenNotFoundException extends Exception {

    public JwtTokenNotFoundException(String params) {

        super(params);
    }

}