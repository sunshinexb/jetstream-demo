package com.example.nats.core.consumer;

import com.example.nats.core.config.JetStreamConsumerDefinition;
import com.example.nats.core.config.NatsJetStreamFrameworkProperties;
import com.example.nats.core.handler.DomainEventHandlerRegistry;
import com.example.nats.core.serialization.EventClassResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class PullConsumerRunner extends BaseConsumerSupport implements AutoCloseable {

    private final Connection connection;
    private JetStreamSubscription subscription;
    private ExecutorService pool;
    private volatile boolean running = true;

    public PullConsumerRunner(Connection connection,
                              JetStreamConsumerDefinition def,
                              NatsJetStreamFrameworkProperties.Defaults defaults,
                              DomainEventHandlerRegistry registry,
                              ObjectMapper mapper,
                              EventClassResolver classResolver) {
        super(def, defaults, registry, mapper, classResolver);
        this.connection = connection;
    }

    public void start() throws Exception {
        JetStream js = connection.jetStream();
        var pullSettings = def.getPull() != null ? def.getPull() : defaults.getPull();

        DeliverPolicy dp = "new".equalsIgnoreCase(effectiveDeliverPolicy()) ? DeliverPolicy.New : DeliverPolicy.All;

        io.nats.client.api.ConsumerConfiguration cc = ConsumerConfiguration.builder()
                .durable(def.getDurable())
                .ackPolicy(AckPolicy.Explicit)
                .ackWait(Duration.ofSeconds(effectiveAckWaitSeconds()))
                .maxDeliver(effectiveMaxDeliver())
                .deliverPolicy(dp)
                .filterSubject(def.getFilterSubject())
                .build();

        PullSubscribeOptions options = PullSubscribeOptions.builder()
                .stream(def.getStream())
                .configuration(cc)
                .build();

        subscription = js.subscribe(def.getFilterSubject(), options);

        pool = Executors.newFixedThreadPool(pullSettings.getWorkerThreads(), r -> {
            Thread t = new Thread(r);
            t.setName("pull-consumer-" + def.getName() + "-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        for (int i = 0; i < pullSettings.getWorkerThreads(); i++) {
            pool.submit(() -> loop(pullSettings));
        }
    }

    private void loop(com.example.nats.core.config.PullSettings ps) {
        while (running) {
            try {
                subscription.pull(ps.getBatch());
                long end = System.currentTimeMillis() + ps.getMaxWaitMs();
                int received = 0;
                while (received < ps.getBatch() && System.currentTimeMillis() < end) {
                    Message msg = subscription.nextMessage(Duration.ofMillis(200));
                    if (msg == null) continue;
                    received++;
                    process(msg);
                }
                if (received == 0) { Thread.sleep(ps.getIdleSleepMs()); }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                try { Thread.sleep(500);} catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
        }
    }

    @Override
    public void close() {
        running = false;
        if (pool != null) pool.shutdownNow();
    }
}
