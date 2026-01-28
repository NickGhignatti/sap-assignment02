package org.example;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order service that uses SAGA pattern for distributed transaction management.
 * Instead of directly sending to RabbitMQ, it initiates a SAGA.
 */
@Service
public class OrderService {
    private final OrderSagaOrchestrator sagaOrchestrator;
    private final Counter orderCounter;

    public OrderService(OrderSagaOrchestrator sagaOrchestrator, MeterRegistry registry) {
        this.sagaOrchestrator = sagaOrchestrator;
        this.orderCounter = Counter.builder("business_orders_created_total")
                .description("Total numbers of orders created")
                .register(registry);
    }

    public OrderResponse createOrder(String customerId, String fromAddress, String toAddress,
                                     double packageWeight, LocalDateTime requestedDeliveryTime,
                                     int maxDeliveryTimeMinutes) {

        String orderId = UUID.randomUUID().toString();

        // Start SAGA instead of direct message
        String sagaId = sagaOrchestrator.startOrderSaga(
                orderId,
                customerId,
                fromAddress,
                toAddress,
                packageWeight,
                requestedDeliveryTime,
                maxDeliveryTimeMinutes
        );

        this.orderCounter.increment();

        return new OrderResponse(
                orderId,
                sagaId,
                customerId,
                fromAddress,
                toAddress,
                packageWeight,
                requestedDeliveryTime,
                maxDeliveryTimeMinutes,
                "SAGA_STARTED"
        );
    }

    public OrderSagaState getOrderStatus(String orderId) {
        return sagaOrchestrator.getSagaByOrderId(orderId);
    }
}

/**
 * Response with SAGA information
 */
record OrderResponse(
        String orderId,
        String sagaId,
        String customerId,
        String fromAddress,
        String toAddress,
        double packageWeight,
        LocalDateTime requestedDeliveryTime,
        int maxDeliveryTimeMinutes,
        String status
) {}