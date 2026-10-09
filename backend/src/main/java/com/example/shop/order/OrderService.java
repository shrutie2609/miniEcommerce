
package com.example.shop.order;

import com.example.shop.exception.InsufficientStockException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.product.Product;
import com.example.shop.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log =
            LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // @Transactional: if any item fails, the whole order
    // (and stock changes) roll back
    @Transactional
    public Order placeOrder(OrderRequest request) {

        log.info(
                "Order creation started for customer {}",
                request.customerEmail()
        );

        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.Line line : request.items()) {

            log.info(
                    "Checking product {} with requested quantity {}",
                    line.productId(),
                    line.quantity()
            );

            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> {

                        log.warn(
                                "Product {} not found while creating order",
                                line.productId()
                        );

                        return new ResourceNotFoundException(
                                "Product " + line.productId() + " not found"
                        );
                    });

            log.info(
                    "Product {} found with stock {}",
                    product.getId(),
                    product.getStock()
            );

            if (product.getStock() < line.quantity()) {

                log.warn(
                        "Insufficient stock for product {}. Requested: {}, Available: {}",
                        product.getId(),
                        line.quantity(),
                        product.getStock()
                );

                throw new InsufficientStockException(
                        "Only " + product.getStock()
                                + " left for " + product.getName()
                );
            }

            product.setStock(
                    product.getStock() - line.quantity()
            );

            log.info(
                    "Stock updated for product {}. Remaining stock: {}",
                    product.getId(),
                    product.getStock()
            );

            order.getItems().add(
                    new OrderItem(
                            product.getId(),
                            product.getName(),
                            line.quantity(),
                            product.getPrice()
                    )
            );

            total = total.add(
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(line.quantity())
                            )
            );
        }

        order.setTotal(total);

        Order saved = orderRepository.save(order);

        log.info(
                "Order {} placed successfully by {} with total {}",
                saved.getId(),
                saved.getCustomerEmail(),
                saved.getTotal()
        );

        return saved;
    }

    public List<Order> getAll() {

        log.info("Fetching all orders");

        List<Order> orders =
                orderRepository.findAllByOrderByCreatedAtDesc();

        log.info("Fetched {} orders", orders.size());

        return orders;
    }
}

