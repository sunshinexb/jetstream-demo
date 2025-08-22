package com.example.nats.core.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    String getEventId();
    void setEventId(String id);

    long getTimestamp();
    void setTimestamp(long ts);

    String getDomain();
    void setDomain(String domain);

    String getAction();
    void setAction(String action);

    static String newId() { return UUID.randomUUID().toString(); }

    default void initIfNeeded(String domain, String action) {
        if (getEventId() == null) setEventId(newId());
        if (getTimestamp() == 0) setTimestamp(Instant.now().toEpochMilli());
        if (getDomain() == null) setDomain(domain);
        if (getAction() == null) setAction(action);
    }
}
