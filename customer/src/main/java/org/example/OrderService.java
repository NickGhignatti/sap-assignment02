package org.example;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {
    private final RabbitTemplate rabbitTemplate;
    private final Counter orderCounter;

    public OrderService(RabbitTemplate rabbitTemplate, MeterRegistry registry) {
        this.rabbitTemplate = rabbitTemplate;

        this.orderCounter = Counter.builder("business_orders_created_total").description("Total numbers of orders created").register(registry);
    }

    public OrderMessage createOrder(String customerId, String fromAddress, String toAddress,
                                    double packageWeight, LocalDateTime requestedDeliveryTime,
                                    int maxDeliveryTimeMinutes) {

        String orderId = UUID.randomUUID().toString();

        OrderMessage order = new OrderMessage(
                orderId,
                customerId,
                fromAddress,
                toAddress,
                packageWeight,
                requestedDeliveryTime,
                maxDeliveryTimeMinutes
        );

        // Send to RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMqConfig.ORDER_QUEUE, order);

        this.orderCounter.increment();

        return order;
    }
}
