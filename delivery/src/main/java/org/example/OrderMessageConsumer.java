package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderMessageConsumer.class);

    @RabbitListener(queues = RabbitMqConfig.ORDER_QUEUE)
    public void processOrderMessage(OrderMessage orderMessage) {
        try {
            logger.info("Received order message: {}", orderMessage);

            if (orderMessage.orderId() == null || orderMessage.orderId().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }

            processDelivery(orderMessage);

            logger.info("Successfully processed order: {}", orderMessage.orderId());

        } catch (Exception e) {
            logger.error("Error processing order message: {}", orderMessage, e);
            throw e;
        }
    }

    private void processDelivery(OrderMessage order) {
        logger.info("Processing delivery for order: {}", order.orderId());
        logger.info("Route: {} -> {}", order.fromAddress(), order.toAddress());
        logger.info("Package details: {}kg, delivery by {}",
                order.packageWeight(), order.requestedDeliveryTime());
    }
}
