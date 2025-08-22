package com.example.nats.core.config;

import io.nats.client.api.StorageType;
import lombok.Data;

import java.util.List;

@Data
public class JetStreamStreamDefinition {
    private String name;
    private List<String> subjects;
    private StorageType storage = StorageType.File;
}
