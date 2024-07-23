package com.thitsaworks.mojaloop.thitsaconnect.jws;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.thitsaworks.mojaloop.thitsaconnect.jws")
public class JwsConfiguration {

    @Bean
    public JwsConfiguration.Settings jwsConfigurationSettings() {

        return new Settings(Boolean.valueOf(System.getProperty("isEnableJws")));

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Settings {

        private Boolean isEnableJws;

    }
}
