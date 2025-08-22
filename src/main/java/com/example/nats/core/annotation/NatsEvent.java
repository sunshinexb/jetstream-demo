package com.example.nats.core.annotation;

import com.example.nats.core.event.DomainEvent;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NatsEvent {
    Class<? extends DomainEvent> value() default DomainEvent.class;
    String subject() default "";
    String domain() default "";
    String action() default "";
    int order() default 0;
}
