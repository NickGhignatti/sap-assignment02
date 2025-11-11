package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DroneMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DroneMessageConsumer.class);
    private final DroneController controller;

    public DroneMessageConsumer(final DroneController controller) {
        this.controller = controller;
    }

    @RabbitListener(queues = RabbitMqConfig.DRONE_QUEUE)
    public void processOrderMessage(OrderMessage orderMessage) {
        try {
            if (orderMessage.orderId() == null || orderMessage.orderId().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }

            Thread thread = new Thread(() -> {
                Drone drone = new Drone(orderMessage);
                drone.start();
                controller.attachDrone(orderMessage.orderId(), drone);

                Random rand = new Random();
                try {
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
