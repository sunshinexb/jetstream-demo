package com.example.nats.core.init;

import com.example.nats.core.config.NatsJetStreamFrameworkProperties;
import io.nats.client.Connection;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StreamConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JetStreamStreamsInitializer implements SmartInitializingSingleton {

    private final Connection connection;
    private final NatsJetStreamFrameworkProperties props;

    @Override
    public void afterSingletonsInstantiated() {
        try {
            if (props.getStreams() == null) return;
            JetStreamManagement jsm = connection.jetStreamManagement();
            for (var def : props.getStreams()) {
                try {
                    jsm.getStreamInfo(def.getName());
                    log.info("Stream '{}' already exists.", def.getName());
                } catch (Exception e) {
                    StreamConfiguration sc = StreamConfiguration.builder()
                            .name(def.getName())
                            .subjects(def.getSubjects())
                            .storageType(def.getStorage())
                            .retentionPolicy(RetentionPolicy.Limits)
                            .build();
                    jsm.addStream(sc);
                    log.info("Created stream '{}' subjects={} ", def.getName(), def.getSubjects());
                }
            }
        } catch (Exception e) {
            log.error("Failed to init streams", e);
        }
    }
}
