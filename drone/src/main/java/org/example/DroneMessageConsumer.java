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
            logger.info("Received order message: {}", orderMessage);
            Random rand = new Random();
            int sleepMinutes = rand.nextInt(orderMessage.maxDeliveryTimeMinutes()) + 1;

            droneService.startDroneDelivery(orderMessage, sleepMinutes);

        } catch (Exception e) {
            logger.error("Error processing order message", e);
            throw e;
        }
    }
}