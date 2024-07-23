package com.thitsaworks.mojaloop.thitsaconnect.component;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.event.DomainEventPublisher;
import com.thitsaworks.mojaloop.thitsaconnect.component.event.publisher.SpringDomainEventPublisher;
import com.thitsaworks.mojaloop.thitsaconnect.component.security.JasyptCrypto;
import com.thitsaworks.mojaloop.thitsaconnect.component.spring.SpringContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.support.TaskUtils;

@ComponentScan("com.thitsaworks.mojaloop.thitsaconnect.component")
public class ComponentConfiguration {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Bean
    public JasyptCrypto jasyptCrypto() {

        return new JasyptCrypto("JASYPT_PASSWORD");
    }

    @Bean
    public SpringContext springContext() {

        return new SpringContext();

    }

    @Bean
    public ObjectMapper objectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.findAndRegisterModules();
        objectMapper.configure(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature(), true);

        return objectMapper;

    }

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {

        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();

        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor("THITSACONNECT - EventThread"));
        eventMulticaster.setErrorHandler(TaskUtils.LOG_AND_SUPPRESS_ERROR_HANDLER);

        return eventMulticaster;

    }

    @Bean
    public DomainEventPublisher domainEventPublisher() {

        DomainEventPublisher domainEventPublisher = new SpringDomainEventPublisher(this.applicationEventPublisher);

        return domainEventPublisher;

    }

}
