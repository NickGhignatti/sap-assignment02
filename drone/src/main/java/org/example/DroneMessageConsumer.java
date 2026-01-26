package org.example;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Consumer that processes order messages and creates drone deliveries
 * using Event Sourcing pattern to track all state changes.
 */
@Service
public class DroneMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DroneMessageConsumer.class);
    private final DroneController controller;
    private final DroneService droneService;
    private final Gauge drones;

    public DroneMessageConsumer(final DroneController controller,
                                final DroneService droneService,
                                MeterRegistry registry) {
        this.controller = controller;
        this.droneService = droneService;
        this.drones = Gauge.builder("drone_sent", controller,
                        c -> c.getCurrentDispatchedDrones().size())
                .description("Total numbers of drones sent")
                .register(registry);
    }

    @RabbitListener(queues = RabbitMqConfig.DRONE_QUEUE)
    public void processOrderMessage(OrderMessage orderMessage) {
        try {
            if (orderMessage.orderId() == null || orderMessage.orderId().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }

            logger.info("Received order message: {}", orderMessage);

            // Process delivery asynchronously using event sourcing
            Thread thread = new Thread(() -> {
                Random rand = new Random();
                int sleepMinutes = rand.nextInt(orderMessage.maxDeliveryTimeMinutes()) + 1;

                logger.info("Processing delivery for order {} (estimated {} minutes)",
                        orderMessage.orderId(), sleepMinutes);

                // Use DroneService which implements event sourcing
                droneService.processDroneDelivery(orderMessage, sleepMinutes);

                logger.info("Completed delivery for order {}", orderMessage.orderId());
            });

            thread.start();

        } catch (Exception e) {
            logger.error("Error processing order message: {}", orderMessage, e);
            throw e;
        }
    }
}