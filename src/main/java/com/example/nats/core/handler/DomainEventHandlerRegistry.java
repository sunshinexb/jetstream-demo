package com.example.nats.core.handler;

import com.example.nats.core.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DomainEventHandlerRegistry {

    private final Map<Class<?>, List<DomainEventHandler<?>>> handlers = new ConcurrentHashMap<>();

    public DomainEventHandlerRegistry(Optional<List<DomainEventHandler<?>>> handlerBeans) {
        handlerBeans.ifPresent(list -> {
            for (DomainEventHandler<?> h : list) {
                handlers.computeIfAbsent(h.eventClass(), k -> new ArrayList<>()).add(h);
                log.info("Registered handler {} for {}", h.getClass().getSimpleName(), h.eventClass().getSimpleName());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <E extends DomainEvent> List<DomainEventHandler<E>> getHandlers(Class<E> clazz) {
        return (List<DomainEventHandler<E>>) (List<?>) handlers.getOrDefault(clazz, Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    public synchronized <E extends DomainEvent> void register(Class<E> clazz, DomainEventHandler<?> handler) {
        handlers.computeIfAbsent(clazz, k -> new ArrayList<>()).add(handler);
        log.info("Registered handler {} via annotation for {}", handler.getClass().getSimpleName(), clazz.getSimpleName());
    }
}
