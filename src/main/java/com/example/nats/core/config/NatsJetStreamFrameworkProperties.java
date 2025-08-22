package com.example.nats.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "nats")
public class NatsJetStreamFrameworkProperties {
    private String servers;
    private List<JetStreamStreamDefinition> streams;
    private Defaults defaults = new Defaults();
    private List<JetStreamConsumerDefinition> consumers;

    @Data
    public static class Defaults {
        private int ackWaitSeconds = 30;
        private int maxDeliver = 5;
        private String deliverPolicy = "all";
        private PullSettings pull = new PullSettings();
        private PushSettings push = new PushSettings();
    }
}
