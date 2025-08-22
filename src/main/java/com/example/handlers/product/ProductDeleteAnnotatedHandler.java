package com.example.handlers.product;

import com.example.events.product.ProductDeleteEvent;
import com.example.nats.core.annotation.NatsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductDeleteAnnotatedHandler {

    @NatsEvent(subject = "product.delete", value = ProductDeleteEvent.class)
    public void onProductDelete(ProductDeleteEvent e) {
        log.info("[@NatsEvent] product.delete productId={} reason={}", e.getProductId(), e.getReason());
    }
}
