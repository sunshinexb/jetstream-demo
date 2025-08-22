package com.example;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JetStreamDemoApplication implements DisposableBean {

    @Value("${nats.servers}")
    private String natsServers;

    private Connection connection;

    public static void main(String[] args) {
        SpringApplication.run(JetStreamDemoApplication.class, args);
    }

    @Bean
    public Connection natsConnection() throws Exception {
        connection = Nats.connect(natsServers);
        return connection;
    }

    @Override
    public void destroy() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
