package com.example.shop.order;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log =
            LoggerFactory.getLogger(OrderController.class);

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order place(@Valid @RequestBody OrderRequest request) {

        log.info("Order placement request received");

        Order order = service.placeOrder(request);

        log.info("Order placement request completed successfully");

        return order;
    }

    @GetMapping
    public List<Order> getAll() {

        log.info("Fetching all orders");

        List<Order> orders = service.getAll();

        log.info("Fetched {} orders", orders.size());

        return orders;
    }
}

