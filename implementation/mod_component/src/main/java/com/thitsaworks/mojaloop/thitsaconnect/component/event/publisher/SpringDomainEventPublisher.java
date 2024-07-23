package com.thitsaworks.mojaloop.thitsaconnect.component.event.publisher;

import com.thitsaworks.mojaloop.thitsaconnect.component.event.DomainEvent;
import com.thitsaworks.mojaloop.thitsaconnect.component.event.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;


public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {

        super();
        this.publisher = publisher;

    }

    @Override
    public void publish(DomainEvent event) {

        this.publisher.publishEvent(event);

    }

}