package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect;

import com.thitsaworks.mojaloop.thitsaconnect.component.ComponentConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.RedisConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.InterledgerConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.jws.JwsConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.ThitsaconnectToIsisConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@ComponentScan("com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect")
@Import(
    value = {
        ComponentConfiguration.class, ThitsaconnectToHubConfiguration.class, RedisConfiguration.class,
        ThitsaconnectToIsisConfiguration.class, InterledgerConfiguration.class,
        JwsConfiguration.class})
public class HubToThitsaconnectConfiguration {

    @Bean
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(30);
        executor.setThreadNamePrefix("MyExecutor-");
        executor.initialize();
        return
            executor;
    }

}
