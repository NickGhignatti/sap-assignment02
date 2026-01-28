package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Order controller with SAGA pattern support
 */
@RestController
@RequestMapping("/")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        LocalDateTime deliveryTime = request.requestedDeliveryTime() != null
                ? request.requestedDeliveryTime()
                : LocalDateTime.now().plusHours(2);

        OrderResponse response = orderService.createOrder(
                request.customerId(),
                request.fromAddress(),
                request.toAddress(),
                request.packageWeight(),
                deliveryTime,
                request.maxDeliveryTimeMinutes()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get SAGA status for an order
     */
    @GetMapping("/{orderId}/saga-status")
    public ResponseEntity<SagaStatusResponse> getSagaStatus(@PathVariable String orderId) {
        OrderSagaState saga = orderService.getOrderStatus(orderId);

        if (saga == null) {
            return ResponseEntity.notFound().build();
        }

        SagaStatusResponse response = new SagaStatusResponse(
                saga.getSagaId(),
                saga.getOrderId(),
                saga.getStatus().toString(),
                saga.getCurrentStep().toString(),
                saga.getCompletedSteps().stream()
                        .map(Enum::toString)
                        .toList(),
                saga.getFailureReason(),
                saga.getStartTime(),
                saga.getEndTime()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer Service is running");
    }
}

/**
 * DTO for SAGA status response
 */
record SagaStatusResponse(
        String sagaId,
        String orderId,
        String status,
        String currentStep,
        java.util.List<String> completedSteps,
        String failureReason,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}