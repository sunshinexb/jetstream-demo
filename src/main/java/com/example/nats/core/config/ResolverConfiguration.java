package com.example.nats.core.config;

import com.example.nats.core.serialization.AnnotationAwareEventClassResolver;
import com.example.nats.core.serialization.ConventionEventClassResolver;
import com.example.nats.core.serialization.EventClassResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResolverConfiguration {

    @Bean
    public EventClassResolver eventClassResolver() {
        return new AnnotationAwareEventClassResolver(
                new ConventionEventClassResolver("com.example.events")
        );
    }
}