package com.example.nats.core.config;

import lombok.Data;

@Data
public class PushSettings {
    private int workerThreads = 4;
    private String queueGroup = "";
    private boolean flowControl = false;
    private long idleHeartbeatMs = 15000;
}
