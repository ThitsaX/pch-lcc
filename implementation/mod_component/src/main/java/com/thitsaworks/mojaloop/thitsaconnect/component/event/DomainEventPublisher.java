package com.thitsaworks.mojaloop.thitsaconnect.component.event;

public interface DomainEventPublisher {

    <E extends DomainEvent> void publish(E event);

}
