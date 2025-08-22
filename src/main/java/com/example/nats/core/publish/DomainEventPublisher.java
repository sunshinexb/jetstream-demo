package com.example.nats.core.publish;

import com.example.nats.core.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final Connection connection;
    private final ObjectMapper mapper = new ObjectMapper();

    public void publish(DomainEvent event) throws Exception {
        String subject = event.getDomain() + "." + event.getAction();
        event.initIfNeeded(event.getDomain(), event.getAction());
        JetStream js = connection.jetStream();
        js.publish(subject, mapper.writeValueAsBytes(event));
    }
}
