package com.example.nats.core.serialization;

public interface EventClassResolver {
    Class<?> resolve(String domain, String action);
}
