package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderMessage> createOrder(@RequestBody CreateOrderRequest request) {
        LocalDateTime deliveryTime = request.requestedDeliveryTime() != null
                ? request.requestedDeliveryTime()
                : LocalDateTime.now().plusHours(2);

        OrderMessage order = orderService.createOrder(
                request.customerId(),
                request.fromAddress(),
                request.toAddress(),
                request.packageWeight(),
                deliveryTime,
                request.maxDeliveryTimeMinutes()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer Service is running");
    }
}
