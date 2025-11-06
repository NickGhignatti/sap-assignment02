package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeliveryService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);
    private final RabbitTemplate rabbitTemplate;

    public DeliveryService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void askDroneForOrder(String customerId, String fromAddress, String toAddress,
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

        rabbitTemplate.convertAndSend(RabbitMqConfig.DRONE_QUEUE, order);

        logger.info("Order created and sent to drone queue: {}", order);

    }
}
