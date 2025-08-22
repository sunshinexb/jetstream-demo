package com.example.events.order;

import com.example.nats.core.event.AbstractBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCreateEvent extends AbstractBaseEvent {
    private String orderId;
    private String userId;
    private long amount;
}
