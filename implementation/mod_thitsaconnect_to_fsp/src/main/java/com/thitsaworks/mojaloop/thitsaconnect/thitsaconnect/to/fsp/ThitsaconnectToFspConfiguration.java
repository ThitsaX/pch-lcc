package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp;

import com.thitsaworks.mojaloop.thitsaconnect.component.ComponentConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp")
@Import(value = {
        ComponentConfiguration.class
})
public class ThitsaconnectToFspConfiguration {

}
