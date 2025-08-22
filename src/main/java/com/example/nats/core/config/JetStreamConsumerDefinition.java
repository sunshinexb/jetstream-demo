package com.example.nats.core.config;

import lombok.Data;

@Data
public class JetStreamConsumerDefinition {
    private String name;
    private String stream;
    private JetStreamConsumerMode mode;
    private String durable;
    private String filterSubject;
    private Integer ackWaitSeconds;
    private Integer maxDeliver;
    private String deliverPolicy;
    private PullSettings pull;
    private PushSettings push;
}
