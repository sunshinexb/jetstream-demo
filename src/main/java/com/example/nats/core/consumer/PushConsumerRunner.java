package com.example.nats.core.consumer;

import com.example.nats.core.config.JetStreamConsumerDefinition;
import com.example.nats.core.config.NatsJetStreamFrameworkProperties;
import com.example.nats.core.handler.DomainEventHandlerRegistry;
import com.example.nats.core.serialization.EventClassResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.PushSubscribeOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class PushConsumerRunner extends BaseConsumerSupport implements AutoCloseable {

    private final Connection connection;
    private JetStreamSubscription subscription;
    private Dispatcher dispatcher;
    private ExecutorService workers;

    public PushConsumerRunner(Connection connection,
                              JetStreamConsumerDefinition def,
                              NatsJetStreamFrameworkProperties.Defaults defaults,
                              DomainEventHandlerRegistry registry,
                              ObjectMapper mapper,
                              EventClassResolver classResolver) {
        super(def, defaults, registry, mapper, classResolver);
        this.connection = connection;
    }

    public void start() throws Exception {
        var pushSettings = def.getPush() != null ? def.getPush() : defaults.getPush();
        JetStream js = connection.jetStream();

        DeliverPolicy dp = "new".equalsIgnoreCase(effectiveDeliverPolicy()) ? DeliverPolicy.New : DeliverPolicy.All;

        ConsumerConfiguration.Builder ccb = ConsumerConfiguration.builder()
                .durable(def.getDurable())
                .ackPolicy(AckPolicy.Explicit)
                .ackWait(Duration.ofSeconds(effectiveAckWaitSeconds()))
                .maxDeliver(effectiveMaxDeliver())
                .deliverPolicy(dp)
                .filterSubject(def.getFilterSubject());

        if (pushSettings.isFlowControl()) {
            ccb.flowControl(pushSettings.getIdleHeartbeatMs());
        }

        ConsumerConfiguration cc = ccb.build();
        PushSubscribeOptions opts = PushSubscribeOptions.builder()
                .stream(def.getStream())
                .configuration(cc)
                .build();

        workers = Executors.newFixedThreadPool(pushSettings.getWorkerThreads(), r -> {
            Thread t = new Thread(r);
            t.setName("push-consumer-" + def.getDurable() + "-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        dispatcher = connection.createDispatcher();
        MessageHandler handler = msg -> {
            if (!msg.isJetStream()) return;
            try { workers.submit(() -> process(msg)); } catch (Exception ex) { msg.nak(); }
        };

        if (StringUtils.hasText(pushSettings.getQueueGroup())) {
            subscription = js.subscribe(def.getFilterSubject(), pushSettings.getQueueGroup(), dispatcher, handler, false, opts);
        } else {
            subscription = js.subscribe(def.getFilterSubject(), dispatcher, handler, false, opts);
        }
    }

    @Override
    public void close() {
        if (subscription != null) { try { subscription.drain(Duration.ofSeconds(2)); } catch (Exception ignored){} }
        if (dispatcher != null) { try { dispatcher.drain(Duration.ofSeconds(1)); } catch (Exception ignored){} }
        if (workers != null) workers.shutdownNow();
    }
}