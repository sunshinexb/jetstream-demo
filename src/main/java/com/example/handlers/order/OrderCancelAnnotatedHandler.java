package com.example.handlers.order;

import com.example.events.order.OrderCancelEvent;
import com.example.nats.core.annotation.NatsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCancelAnnotatedHandler {

    @NatsEvent(domain = "order", action = "cancel")
    public boolean onOrderCancel(OrderCancelEvent e) {
        log.info("[@NatsEvent] order.cancel orderId={} reason={}", e.getOrderId(), e.getReason());
        return true;
    }
}
