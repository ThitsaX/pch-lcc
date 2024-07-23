package com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security;

import com.thitsaworks.mojaloop.thitsaconnect.component.http.CachedBodyHttpServletRequest;
import com.thitsaworks.mojaloop.thitsaconnect.component.util.JsonWebToken;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.FspConnectorApiConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.exception.AuthenticationFailureException;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.exception.JwtTokenNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ApiAuthenticator implements Authenticator {

    private final static Logger LOG = LoggerFactory.getLogger(ApiAuthenticator.class);

    @Autowired
    FspConnectorApiConfiguration.KeySettings keySettings;

    private final String publicKey;

    @Autowired
    public ApiAuthenticator(FspConnectorApiConfiguration.KeySettings keySettings) {

        this.keySettings = keySettings;
        this.publicKey = this.keySettings.getPublicKey();
    }

    @Override
    public FspContext authenticate(CachedBodyHttpServletRequest cachedBodyRequest)
        throws AuthenticationFailureException, JwtTokenNotFoundException {

        final String TOKEN_PREFIX = "JWT ";

        String authorizationHeader = cachedBodyRequest.getHeader("Authorization");

        if (StringUtils.isBlank(authorizationHeader) || !authorizationHeader.startsWith(TOKEN_PREFIX)) {

            throw new JwtTokenNotFoundException("JWT token not found in the Authorization header.");
        }

        String jwtToken = StringUtils.substringAfter(authorizationHeader, TOKEN_PREFIX);
        LOG.info("JWT token: [{}]", jwtToken);

        try {

            String requestBody = new String(cachedBodyRequest.getCachedBody(), StandardCharsets.UTF_8);

            boolean isAuthenticated = JsonWebToken.verifyUsingPublicKey(this.publicKey, jwtToken);

            if (isAuthenticated) {

                LOG.info("JWT Authorization header validation is successful.");

                boolean isValidPayload = this.payloadVerification(jwtToken,
                                                                  requestBody.replace("\r",
                                                                                      ""));//requestBody.replace("\r","")

                if (isValidPayload) {

                    LOG.info("JWT Authorization payload validation is successful.");

                    return new FspContext(true);

                } else {

                    LOG.info("JWT Authorization payload validation is fail.");

                    throw new AuthenticationFailureException("Authentication failure. Invalid payload.");
                }

            } else {

                throw new AuthenticationFailureException(
                    "Authentication failure. Cannot reproduce Auth-Header at backend.");

            }

        } catch (Exception e) {

            LOG.error("Error verifying JWT token", e);

            throw new AuthenticationFailureException("Error verifying JWT token.");

        }

    }

    private boolean payloadVerification(String token, String payload) {

        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String decodedPayload = new String(decoder.decode(chunks[1]));

        return decodedPayload.equals(payload);
    }

}
