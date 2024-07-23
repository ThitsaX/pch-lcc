package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis;

import com.thitsaworks.mojaloop.thitsaconnect.component.ComponentConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitService;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.ThitsaconnectToFspConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.IsisFspService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis")
@Import(
    value = {
        ComponentConfiguration.class, ThitsaconnectToFspConfiguration.class})
public class ThitsaconnectToIsisConfiguration {

    @Bean
    public ThitsaconnectToIsisConfiguration.Settings settings() {

        Settings settings = new Settings();
        settings.setIsIsUrl(System.getProperty("isisUrl"));
        settings.setUsername(System.getProperty("isisUserName"));
        settings.setPassword(System.getProperty("isisPassword"));
        return settings;

    }

    @Bean
    public IsisFspService isisFspService(ThitsaconnectToIsisConfiguration.Settings settings) {

        return new RetrofitService<>(IsisFspService.class, settings.getIsIsUrl(), null, true).getService();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Settings {

        private String isIsUrl;

        private String username;

        private String password;

    }

}
