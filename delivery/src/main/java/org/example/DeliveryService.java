package org.example;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliveryService {
    private final RabbitTemplate rabbitTemplate;

    public DeliveryService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void askDroneForOrder(final String orderId,
                                 final String customerId,
                                 final String fromAddress,
                                 final String toAddress,
                                 final double packageWeight,
                                 final LocalDateTime requestedDeliveryTime,
                                 final int maxDeliveryTimeMinutes) {

        OrderMessage order = new OrderMessage(orderId, customerId, fromAddress, toAddress,
                packageWeight, requestedDeliveryTime, maxDeliveryTimeMinutes);

        rabbitTemplate.convertAndSend(RabbitMqConfig.DRONE_QUEUE, order);
    }
}
