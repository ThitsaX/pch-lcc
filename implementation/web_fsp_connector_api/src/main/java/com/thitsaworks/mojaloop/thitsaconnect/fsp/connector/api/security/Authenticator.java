package com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security;

import com.thitsaworks.mojaloop.thitsaconnect.component.http.CachedBodyHttpServletRequest;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.exception.AuthenticationFailureException;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.exception.JwtTokenNotFoundException;

public interface Authenticator {

    FspContext authenticate(CachedBodyHttpServletRequest request)
        throws AuthenticationFailureException, JwtTokenNotFoundException;

}
