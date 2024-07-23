package com.thitsaworks.mojaloop.thitsaconnect.interledger;

import com.thitsaworks.mojaloop.thitsaconnect.component.ComponentConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.thitsaworks.mojaloop.thitsaconnect.interledger")
@Import(value = {
        ComponentConfiguration.class
})
public class InterledgerConfiguration {

    @Bean
    public InterledgerConfiguration.Settings interledgerConfigurationSettings() {

        return new Settings(System.getProperty("thitsaconnectKey"));

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Settings {

        private String thitsaconnectKey;

    }

}
