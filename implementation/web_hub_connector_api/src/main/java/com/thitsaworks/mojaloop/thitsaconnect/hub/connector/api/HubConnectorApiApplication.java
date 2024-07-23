package com.thitsaworks.mojaloop.thitsaconnect.hub.connector.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HubConnectorApiApplication {

    public static void main(String[] args) {

        SpringApplication.run(HubConnectorApiApplication.class, args);
    }

}
