package com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api;

import com.thitsaworks.mojaloop.thitsaconnect.fsp.to.thitsaconnect.FspToThitsaconnectConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(
    basePackages = {
        " com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api",
        "com.thitsaworks.mojaloop.thitsaconnect.fsp.to.thitsaconnect"})
@Import(
    value = {
        FspToThitsaconnectConfiguration.class, WebConfiguration.class
    })
public class FspConnectorApiConfiguration {

    @Bean
    public KeySettings keySettings() {

        return new KeySettings(System.getProperty("publicKey"));
    }

    @Getter
    @AllArgsConstructor
    public static class KeySettings {

        private String publicKey;

    }

}
