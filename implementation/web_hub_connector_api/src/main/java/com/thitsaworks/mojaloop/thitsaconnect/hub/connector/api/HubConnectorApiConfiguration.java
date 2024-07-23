package com.thitsaworks.mojaloop.thitsaconnect.hub.connector.api;

import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.HubToThitsaconnectConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {
        "com.thitsaworks.mojaloop.thitsaconnect.hub.connector.api",
        "com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect"})
@Import(value = {
        HubToThitsaconnectConfiguration.class, WebConfiguration.class, WebSecurityConfiguration.class})
public class HubConnectorApiConfiguration {

}
