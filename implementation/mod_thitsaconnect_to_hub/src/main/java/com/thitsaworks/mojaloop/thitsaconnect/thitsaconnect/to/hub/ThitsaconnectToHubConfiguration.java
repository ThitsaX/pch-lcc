package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub;

import com.thitsaworks.mojaloop.thitsaconnect.component.ComponentConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub")
@Import(value = {
        ComponentConfiguration.class})
public class ThitsaconnectToHubConfiguration {

    @Bean
    public ThitsaconnectToHubConfiguration.Settings fspiopConfigurationSettings() {

        return new Settings(System.getProperty("fspiopBaseUrl"));

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Settings {

        private String fspiopBaseUrl;

    }

}
