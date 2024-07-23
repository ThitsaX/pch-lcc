package com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security;

import com.thitsaworks.mojaloop.thitsaconnect.component.http.CachedBodyHttpServletRequest;
import com.thitsaworks.mojaloop.thitsaconnect.component.usecase.UseCaseContext;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.exception.ApiSecurityException;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.exception.AuthenticationFailureException;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.exception.JwtTokenNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiAuthenticationTokenFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private Authenticator authenticator = null;

    public ApiAuthenticationTokenFilter(Authenticator authenticator) {

        super();
        this.authenticator = authenticator;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        LOG.info("Authorization Header : {}", authHeader);

        String requestUri = request.getRequestURI();

        if (!requestUri.startsWith("/secured")) {

            LOG.info("No authentication required.");
            filterChain.doFilter(request, response);
            return;
        }

        if ((authHeader == null || authHeader.isEmpty())) {

            LOG.info("No authentication required.");
          
            filterChain.doFilter(request, response);

        } else {

            CachedBodyHttpServletRequest cachedBodyHttpServletRequest =
                new CachedBodyHttpServletRequest((HttpServletRequest) request);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                FspContext fc;

                try {

                    fc = this.authenticator.authenticate(cachedBodyHttpServletRequest);

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken("12", "N.A", authorities);

                    authenticationToken.setDetails(fc);

                    UseCaseContext.set(fc);

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                } catch (AuthenticationFailureException | JwtTokenNotFoundException e) {

                    LOG.error("Error :", e);

                    throw new ApiSecurityException(e);

                }

            }

            filterChain.doFilter(cachedBodyHttpServletRequest, response);

        }

    }

}