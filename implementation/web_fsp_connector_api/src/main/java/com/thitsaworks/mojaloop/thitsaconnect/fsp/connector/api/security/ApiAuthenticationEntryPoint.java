package com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.error.ErrorResponse;
import com.thitsaworks.mojaloop.thitsaconnect.component.spring.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOG = LoggerFactory.getLogger(ApiAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        LOG.info("inside AuthEntryPoint : {}",
                 authException.getClass()
                              .getName());

        LOG.error("Exception : ", authException);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode("403");
        errorResponse.setMessage("Unable to authenticate the request.");
        errorResponse.setLocaleMessage("Unable to authenticate the request.");

        response.getWriter()
                .write(objectMapper.writeValueAsString(errorResponse));

    }

}
