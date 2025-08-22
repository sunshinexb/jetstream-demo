package com.example.nats.core.serialization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationAwareEventClassResolver implements EventClassResolver {

    private final Map<String, Class<?>> mapping = new ConcurrentHashMap<>();
    private final EventClassResolver delegate;

    public AnnotationAwareEventClassResolver(EventClassResolver delegate) { this.delegate = delegate; }

    public void register(String domain, String action, Class<?> eventClass) {
        if (domain == null || action == null || eventClass == null) return;
        mapping.put((domain + "|" + action).toLowerCase(), eventClass);
    }

    @Override
    public Class<?> resolve(String domain, String action) {
        Class<?> c = mapping.get((domain + "|" + action).toLowerCase());
        if (c != null) return c;
        return delegate != null ? delegate.resolve(domain, action) : null;
    }
}
