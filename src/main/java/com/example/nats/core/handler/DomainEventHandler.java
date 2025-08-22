package com.example.nats.core.handler;

import com.example.nats.core.event.DomainEvent;

public interface DomainEventHandler<E extends DomainEvent> {
    Class<E> eventClass();
    void handle(E event) throws Exception;
    default void onRedelivery(E event, int redeliveryCount) {}
}
