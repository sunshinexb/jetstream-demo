package com.example.handlers.order;

import com.example.events.order.OrderCreateEvent;
import com.example.nats.core.handler.DomainEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreateHandler implements DomainEventHandler<OrderCreateEvent> {
    @Override
    public Class<OrderCreateEvent> eventClass() { return OrderCreateEvent.class; }
    @Override
    public void handle(OrderCreateEvent event) {
        log.info("[OrderCreateHandler] orderId={} amount={} user={}",
                event.getOrderId(), event.getAmount(), event.getUserId());
    }
}
