package org.example;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Random;

@Service
public class DroneMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DroneMessageConsumer.class);
    private final DroneController controller;
    private final Gauge drones;

    public DroneMessageConsumer(final DroneController controller, MeterRegistry registry) {
        this.controller = controller;
        this.drones = Gauge.builder("drone_sent", controller, c -> c.getCurrentDispatchedDrones().size()).description("Total numbers of drones sent").register(registry);
    }

    @RabbitListener(queues = RabbitMqConfig.DRONE_QUEUE)
    public void processOrderMessage(OrderMessage orderMessage) {
        try {
            if (orderMessage.orderId() == null || orderMessage.orderId().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }

            logger.info("Received order message: {}", orderMessage);

            Thread thread = new Thread(() -> {
                Drone drone = new Drone(orderMessage);
                drone.start();
                controller.attachDrone(orderMessage.orderId(), drone);

                Random rand = new Random();
                try {
                    logger.info("Sleeping for {} minutes", orderMessage.maxDeliveryTimeMinutes());
                    int sleepMinutes = rand.nextInt(orderMessage.maxDeliveryTimeMinutes()) + 1;
                    Thread.sleep(sleepMinutes * 60_000L);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }

                drone.end();
                controller.detachDrone(drone.getId());
            });

            thread.start();

        } catch (Exception e) {
            logger.error("Error processing order message: {}", orderMessage, e);
            throw e;
        }
    }
}
