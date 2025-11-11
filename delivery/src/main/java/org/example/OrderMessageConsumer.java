package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderMessageConsumer {
    private final DeliveryService deliveryService;
    private static final Logger logger = LoggerFactory.getLogger(OrderMessageConsumer.class);

    public OrderMessageConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_QUEUE)
    public void processOrderMessage(OrderMessage orderMessage) {
        try {
            if (orderMessage.orderId() == null || orderMessage.orderId().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }

            deliveryService.askDroneForOrder(
                    orderMessage.orderId(),
                    orderMessage.customerId(),
                    orderMessage.fromAddress(),
                    orderMessage.toAddress(),
                    orderMessage.packageWeight(),
                    orderMessage.requestedDeliveryTime(),
                    orderMessage.maxDeliveryTimeMinutes()
            );
        } catch (Exception e) {
            logger.error("Error processing order message: {}", orderMessage, e);
            throw e;
        }
    }
}
