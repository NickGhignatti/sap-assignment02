package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Customer Service - Creates orders and sends them to RabbitMQ
 */
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private static final String QUEUE_NAME = "order_queue";
    private static final String RABBITMQ_HOST = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");

    public static void main(String[] args) {
        logger.info("Customer Service starting...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare the queue (idempotent - safe to call multiple times)
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            logger.info("Connected to RabbitMQ and declared queue: {}", QUEUE_NAME);

            // Create an example order
            OrderMessage order = new OrderMessage(
                    UUID.randomUUID().toString(),
                    "CUSTOMER-001",
                    "Via Zamboni 33, Bologna",
                    "Piazza Maggiore, Bologna",
                    2.5,
                    LocalDateTime.now().plusHours(2),
                    30
            );

            // Serialize to JSON bytes
            byte[] messageBytes = MessageSerializer.serialize(order);

            // Send to RabbitMQ
            channel.basicPublish("", QUEUE_NAME, null, messageBytes);

            logger.info("Sent order: {}", order);
            logger.info("JSON: {}", MessageSerializer.serializeToString(order));

            // Keep service running (in real app, this would be a web server)
            logger.info("Customer service running. Press Ctrl+C to exit.");
            Thread.sleep(60000);

        } catch (Exception e) {
            logger.error("Error in Customer Service", e);
        }
    }
}