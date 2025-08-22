package com.example.nats.core.consumer;

import com.example.nats.core.config.JetStreamConsumerDefinition;
import com.example.nats.core.config.JetStreamConsumerMode;
import com.example.nats.core.config.NatsJetStreamFrameworkProperties;
import com.example.nats.core.handler.DomainEventHandlerRegistry;
import com.example.nats.core.serialization.EventClassResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumersBootstrap implements SmartInitializingSingleton, DisposableBean {

    private final Connection connection;
    private final NatsJetStreamFrameworkProperties props;
    private final DomainEventHandlerRegistry registry;
    private final EventClassResolver resolver;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<AutoCloseable> runners = new ArrayList<>();

    @Override
    public void afterSingletonsInstantiated() {
        try {
            if (props.getConsumers() == null) return;
            for (JetStreamConsumerDefinition def : props.getConsumers()) {
                AutoCloseable runner;
                if (def.getMode() == JetStreamConsumerMode.pull) {
                    PullConsumerRunner p = new PullConsumerRunner(connection, def, props.getDefaults(), registry, objectMapper, resolver);
                    p.start();
                    runner = p;
                } else {
                    PushConsumerRunner p = new PushConsumerRunner(connection, def, props.getDefaults(), registry, objectMapper, resolver);
                    p.start();
                    runner = p;
                }
                runners.add(runner);
                log.info("Started {} consumer '{}' on subject '{}'", def.getMode(), def.getName(), def.getFilterSubject());
            }
        } catch (Exception e) {
            log.error("Failed to start consumers", e);
        }
    }

    @Override
    public void destroy() {
        for (AutoCloseable r : runners) {
            try { r.close(); } catch (Exception ignored) { }
        }
    }
}
