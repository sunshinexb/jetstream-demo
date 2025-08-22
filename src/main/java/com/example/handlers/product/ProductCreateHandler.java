package com.example.handlers.product;

import com.example.events.product.ProductCreateEvent;
import com.example.nats.core.handler.DomainEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductCreateHandler implements DomainEventHandler<ProductCreateEvent> {
    @Override
    public Class<ProductCreateEvent> eventClass() { return ProductCreateEvent.class; }
    @Override
    public void handle(ProductCreateEvent event) {
        log.info("[ProductCreateHandler] productId={} name={} category={}",
                event.getProductId(), event.getName(), event.getCategory());
    }
}
