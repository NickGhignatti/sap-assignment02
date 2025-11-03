package org.example;

import com.rabbitmq.client.*;
import org.example.MessageSerializer;
import org.example.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delivery Service - Receives orders from RabbitMQ and processes them
 */
public class DeliveryService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);
    private static final String QUEUE_NAME = "order_queue";
    private static final String RABBITMQ_HOST = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");

    public static void main(String[] args) {
        logger.info("Delivery Service starting...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // Declare the same queue (idempotent)
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            logger.info("Connected to RabbitMQ and listening on queue: {}", QUEUE_NAME);

            // Set up consumer
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    // Deserialize the message bytes back to OrderMessage object
                    OrderMessage order = MessageSerializer.deserialize(
                            delivery.getBody(),
                            OrderMessage.class
                    );

                    logger.info("Received order: {}", order);

                    // Now you can access ALL fields from the original message!
                    logger.info("Processing delivery for customer: {}", order.getCustomerId());
                    logger.info("From: {} to: {}", order.getFromAddress(), order.getToAddress());
                    logger.info("Package weight: {} kg", order.getPackageWeight());
                    logger.info("Max delivery time: {} minutes", order.getMaxDeliveryTimeMinutes());

                    // Process the order (assign drone, calculate route, etc.)
                    processDelivery(order);

                    // Acknowledge message
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                } catch (Exception e) {
                    logger.error("Error processing message", e);
                    // Reject and requeue the message
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                }
            };

            // Start consuming messages
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

            logger.info("Delivery service is running. Waiting for orders...");

            // Keep the service running
            Thread.currentThread().join();

        } catch (Exception e) {
            logger.error("Error in Delivery Service", e);
        }
    }

    private static void processDelivery(OrderMessage order) {
        logger.info("Assigning drone for order: {}", order.getOrderId());
        // Your business logic here:
        // - Find available drone
        // - Calculate optimal route
        // - Send DroneAssignmentMessage to drone service
        // - Update delivery status
    }
}