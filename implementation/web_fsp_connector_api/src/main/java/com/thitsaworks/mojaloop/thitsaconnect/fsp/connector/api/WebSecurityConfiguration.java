package com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api;

import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.ApiAuthenticationEntryPoint;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.ApiAuthenticationTokenFilter;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.ApiAuthenticator;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.AuthFilterExceptionHandler;
import com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.security.Authenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ApiAuthenticationTokenFilter authenticationTokenFilter)
        throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement((sessionManagement)
                                   -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling((exceptionHandling)
                                   -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler()))
            .authorizeHttpRequests(configurer ->
                                       configurer
                                           .antMatchers("/secured/**").authenticated()
                                           .anyRequest().permitAll())
            .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(authFilterExceptionHandler(), ApiAuthenticationTokenFilter.class);
        return http.build();

    }

    @Bean
    public ApiAuthenticationTokenFilter authenticationTokenFilter(Authenticator authenticator) {

        return new ApiAuthenticationTokenFilter(authenticator);
    }

    @Bean
    public Authenticator authenticator(FspConnectorApiConfiguration.KeySettings keySettings) {

        return new ApiAuthenticator(keySettings);

    }

    @Bean
    public AuthFilterExceptionHandler authFilterExceptionHandler() {

        return new AuthFilterExceptionHandler();
    }

    @Bean
    public ApiAuthenticationEntryPoint unauthorizedHandler() {

        return new ApiAuthenticationEntryPoint();
    }

}