package com.example.nats.core.config;

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
