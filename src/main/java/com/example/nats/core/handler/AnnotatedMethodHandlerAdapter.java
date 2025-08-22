package com.example.nats.core.handler;

import com.example.nats.core.event.DomainEvent;

import java.lang.reflect.Method;

public class AnnotatedMethodHandlerAdapter<E extends DomainEvent> implements DomainEventHandler<E> {

    private final Object bean;
    private final Method method;
    private final Class<E> eventClass;
    private final int order;

    public AnnotatedMethodHandlerAdapter(Object bean, Method method, Class<E> eventClass, int order) {
        this.bean = bean;
        this.method = method;
        this.eventClass = eventClass;
        this.order = order;
        this.method.setAccessible(true);
    }

    @Override
    public Class<E> eventClass() { return eventClass; }

    @Override
    public void handle(E event) throws Exception {
        Object ret = method.getParameterCount() == 1 ? method.invoke(bean, event) : method.invoke(bean);
        if (ret instanceof Boolean && !(Boolean) ret) {
            throw new RuntimeException("Annotated handler returned false, treat as failure.");
        }
    }

    public int getOrder() { return order; }
}
