package com.example.nats.core.event;

import lombok.Data;

@Data
public abstract class AbstractBaseEvent implements DomainEvent {
    private String eventId;
    private long timestamp;
    private String domain;
    private String action;
}
