package com.example.nats.core.consumer;

import com.example.nats.core.config.JetStreamConsumerDefinition;
import com.example.nats.core.config.NatsJetStreamFrameworkProperties;
import com.example.nats.core.event.DomainEvent;
import com.example.nats.core.handler.DomainEventHandler;
import com.example.nats.core.handler.DomainEventHandlerRegistry;
import com.example.nats.core.serialization.EventClassResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class BaseConsumerSupport {

    protected final JetStreamConsumerDefinition def;
    protected final NatsJetStreamFrameworkProperties.Defaults defaults;
    protected final DomainEventHandlerRegistry registry;
    protected final ObjectMapper mapper;
    protected final EventClassResolver classResolver;

    protected BaseConsumerSupport(JetStreamConsumerDefinition def,
                                  NatsJetStreamFrameworkProperties.Defaults defaults,
                                  DomainEventHandlerRegistry registry,
                                  ObjectMapper mapper,
                                  EventClassResolver classResolver) {
        this.def = def;
        this.defaults = defaults;
        this.registry = registry;
        this.mapper = mapper;
        this.classResolver = classResolver;
    }

    protected void process(Message msg) {
        try {
            String subject = msg.getSubject();
            String[] parts = subject.split("\\.");
            if (parts.length < 2) { msg.nak(); return; }
            String domain = parts[0];
            String action = parts[1];

            Class<?> clazz = classResolver.resolve(domain, action);
            if (clazz == null) { msg.term(); return; }

            Object obj = mapper.readValue(msg.getData(), clazz);
            if (!(obj instanceof DomainEvent)) { msg.term(); return; }
            DomainEvent event = (DomainEvent) obj;
            event.initIfNeeded(domain, action);

            @SuppressWarnings("unchecked")
            List<DomainEventHandler<DomainEvent>> handlers =
                    (List<DomainEventHandler<DomainEvent>>) (List<?>) registry.getHandlers(event.getClass());
            if (handlers.isEmpty()) { msg.term(); return; }

            long redelivery = 1;
            try { redelivery = msg.metaData()
                    .deliveredCount(); } catch (Exception ignore){}

            for (DomainEventHandler<DomainEvent> h : handlers) {
                if (redelivery > 1) h.onRedelivery(event, redelivery);
                h.handle(event);
            }
            msg.ack();
        } catch (Exception ex) {
            long delivered = 0; try { delivered = msg.metaData().deliveredCount(); } catch (Exception ignore){}
            if (delivered >= effectiveMaxDeliver()) msg.term(); else msg.nak();
        }
    }

    protected int effectiveAckWaitSeconds() {
        return def.getAckWaitSeconds() != null ? def.getAckWaitSeconds() : defaults.getAckWaitSeconds();
    }

    protected int effectiveMaxDeliver() {
        return def.getMaxDeliver() != null ? def.getMaxDeliver() : defaults.getMaxDeliver();
    }

    protected String effectiveDeliverPolicy() {
        return def.getDeliverPolicy() != null ? def.getDeliverPolicy() : defaults.getDeliverPolicy();
    }
}
