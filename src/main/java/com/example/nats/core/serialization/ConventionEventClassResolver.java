package com.example.nats.core.serialization;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConventionEventClassResolver implements EventClassResolver {

    private final String basePackage;
    private final Map<String, Class<?>> cache = new ConcurrentHashMap<>();

    public ConventionEventClassResolver(String basePackage) {
        this.basePackage = basePackage.endsWith(".") ? basePackage : basePackage + ".";
    }

    @Override
    public Class<?> resolve(String domain, String action) {
        if (!StringUtils.hasText(domain) || !StringUtils.hasText(action)) return null;
        String key = domain + ":" + action;
        return cache.computeIfAbsent(key, k -> {
            try {
                String className = basePackage + domain.toLowerCase(Locale.ROOT)
                        + "." + capitalize(domain) + capitalize(action) + "Event";
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
