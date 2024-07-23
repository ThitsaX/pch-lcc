package com.thitsaworks.mojaloop.thitsaconnect.fsp.to.thitsaconnect;

import com.thitsaworks.mojaloop.thitsaconnect.component.ComponentConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.jws.JwsConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan("com.thitsaworks.mojaloop.thitsaconnect.fsp.to.thitsaconnect")
@Import(value = {
        ComponentConfiguration.class, ThitsaconnectToHubConfiguration.class,
        JwsConfiguration.class,
        RedisConfiguration.class
})
public class FspToThitsaconnectConfiguration {

    @Bean
    public FspToThitsaconnectConfiguration.Settings fspToThitsaconnectConfigurationSettings() {

        return new Settings(System.getProperty("localeLanguage"));
    }

    @Getter
    @AllArgsConstructor
    public static class Settings {

        private String localeLanguage;

    }

}
