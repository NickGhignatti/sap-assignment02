package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {
    private final RabbitTemplate rabbitTemplate;

    public OrderService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
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

        return order;
    }
}
