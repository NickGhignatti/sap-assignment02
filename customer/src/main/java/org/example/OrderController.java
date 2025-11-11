package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderMessage> createOrder(@RequestBody CreateOrderRequest request) {
        LocalDateTime deliveryTime = request.getRequestedDeliveryTime() != null
                ? request.getRequestedDeliveryTime()
                : LocalDateTime.now().plusHours(2);

        OrderMessage order = orderService.createOrder(
                request.getCustomerId(),
                request.getFromAddress(),
                request.getToAddress(),
                request.getPackageWeight(),
                deliveryTime,
                request.getMaxDeliveryTimeMinutes()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer Service is running");
    }
}
