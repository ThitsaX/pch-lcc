package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis;

import com.thitsaworks.mojaloop.thitsaconnect.component.ComponentConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.component.retrofit.RetrofitService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis")
@Import(value = {
        ComponentConfiguration.class})
public class RedisConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConfiguration.class);

    @Bean
    public Settings redisConfigurationSettings() {

        return new Settings(System.getProperty("redisUrl"),
                            System.getProperty("cacheLifeTime"));

    }



    @Bean
    public RedissonClient redisClient(RedisConfiguration.Settings settings) {

        Config config = new Config();
        config.useSingleServer().setAddress(settings.getRedisUrl());

        return Redisson.create(config);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Settings {

        private String redisUrl;
        private String cacheLifetime;

    }

}
