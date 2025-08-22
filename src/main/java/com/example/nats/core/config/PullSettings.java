package com.example.nats.core.config;

import lombok.Data;

@Data
public class PullSettings {
    private int batch = 10;
    private long maxWaitMs = 2000;
    private int workerThreads = 2;
    private long idleSleepMs = 200;
}
