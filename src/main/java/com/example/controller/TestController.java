package com.example.controller;

import com.example.events.product.ProductCreateEvent;
import com.example.events.order.OrderCreateEvent;
import com.example.nats.core.publish.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final DomainEventPublisher publisher;

    @PostMapping("/product/create")
    public String productCreate(@RequestParam String id,
                                @RequestParam String name,
                                @RequestParam String category) throws Exception {
        ProductCreateEvent e = new ProductCreateEvent();
        e.setProductId(id);
        e.setName(name);
        e.setCategory(category);
        e.setDomain("product");
        e.setAction("create");
        publisher.publish(e);
        return "product create sent";
    }

    @PostMapping("/order/create")
    public String orderCreate(@RequestParam String id,
                              @RequestParam String user,
                              @RequestParam long amount) throws Exception {
        OrderCreateEvent e = new OrderCreateEvent();
        e.setOrderId(id);
        e.setUserId(user);
        e.setAmount(amount);
        e.setDomain("order");
        e.setAction("create");
        publisher.publish(e);
        return "order create sent";
    }
}
