package com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.error.ErrorResponse;
import com.thitsaworks.mojaloop.thitsaconnect.component.mojaloop.ErrorCode;
import com.thitsaworks.mojaloop.thitsaconnect.component.spring.SpringContext;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.exception.ApiSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilterExceptionHandler extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthFilterExceptionHandler.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException {

        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);

        try {

            LOG.info("inside Exception Handler");
            filterChain.doFilter(request, response);
            LOG.info("finish : inside Exception Handler");

        } catch (ApiSecurityException e) {

            LOG.error("Error :", e);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String localizedMessage =
                    ErrorCode.getMojaloopErrorResponseByStatusCode(ErrorCode.PAYER_PERMISSION_ERROR.getStatusCode()
                                                                                                   .toString(),
                                                                   System.getProperty("localeLanguage"));

            JsonNode jsonNode = objectMapper.readTree(localizedMessage);

            ErrorResponse errorResponse = new ErrorResponse();

            errorResponse.setStatusCode(jsonNode.get("errorInformation").get("statusCode").asText());
            errorResponse.setMessage(jsonNode.get("errorInformation").get("description").asText());
            errorResponse.setLocaleMessage(jsonNode.get("errorInformation").get("descriptionLocale").asText());

            response.getWriter()
                    .write(objectMapper.writeValueAsString(errorResponse));

        } catch (Exception e) {

            LOG.error("Error : ", e);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatusCode("400");
            errorResponse.setMessage(e.getCause().getMessage());
            errorResponse.setLocaleMessage(e.getCause().getLocalizedMessage());

            response.getWriter()
                    .write(objectMapper.writeValueAsString(errorResponse));

        }

    }

}