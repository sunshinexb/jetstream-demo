package com.example.nats.core.annotation;

import com.example.nats.core.event.DomainEvent;
import com.example.nats.core.handler.AnnotatedMethodHandlerAdapter;
import com.example.nats.core.handler.DomainEventHandler;
import com.example.nats.core.handler.DomainEventHandlerRegistry;
import com.example.nats.core.serialization.AnnotationAwareEventClassResolver;
import com.example.nats.core.serialization.EventClassResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnotatedHandlerRegistrar implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final DomainEventHandlerRegistry registry;
    private final EventClassResolver resolver;

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean;
            try { bean = applicationContext.getBean(beanName); } catch (Exception ignore) { continue; }
            Class<?> targetClass = bean.getClass();
            ReflectionUtils.doWithMethods(targetClass, method -> {
                NatsEvent ne = AnnotatedElementUtils.findMergedAnnotation(method, NatsEvent.class);
                if (ne == null) return;

                Class<? extends DomainEvent> eventClass = resolveEventClass(method, ne);
                if (eventClass == null) {
                    log.warn("@NatsEvent on {}.{} missing event class.", targetClass.getSimpleName(), method.getName());
                    return;
                }

                String domain = ne.domain();
                String action = ne.action();
                String subject = ne.subject();
                if (!StringUtils.hasText(subject) && (!StringUtils.hasText(domain) || !StringUtils.hasText(action))) {
                    log.warn("@NatsEvent on {}.{} requires subject or domain+action.", targetClass.getSimpleName(), method.getName());
                    return;
                }
                if (StringUtils.hasText(subject)) {
                    String[] parts = subject.split("\\.");
                    if (parts.length >= 2) { domain = parts[0]; action = parts[1]; }
                    else { log.warn("@NatsEvent subject invalid: {} on {}.{}", subject, targetClass.getSimpleName(), method.getName()); return; }
                }

                if (resolver instanceof AnnotationAwareEventClassResolver) {
                    ((AnnotationAwareEventClassResolver) resolver).register(domain, action, eventClass);
                }

                DomainEventHandler<?> adapter = new AnnotatedMethodHandlerAdapter<>(bean, method, eventClass, ne.order());
                registry.register(eventClass, adapter);

                log.info("Registered @NatsEvent {}.{} -> {}.{} ({}),"
                        , targetClass.getSimpleName(), method.getName(), domain, action, eventClass.getSimpleName());
            });
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> resolveEventClass(Method method, NatsEvent ann) {
        if (ann.value() != null && ann.value() != DomainEvent.class) return ann.value();
        if (method.getParameterCount() == 1 && DomainEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return (Class<? extends DomainEvent>) method.getParameterTypes()[0];
        }
        return null;
    }
}
