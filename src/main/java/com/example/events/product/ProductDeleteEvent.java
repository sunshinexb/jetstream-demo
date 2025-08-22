package com.example.events.product;

import com.example.nats.core.event.AbstractBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDeleteEvent extends AbstractBaseEvent {
    private String productId;
    private String reason;
}
