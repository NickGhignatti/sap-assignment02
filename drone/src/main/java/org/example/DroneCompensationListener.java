package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DroneCompensationListener {
    private static final Logger logger = LoggerFactory.getLogger(DroneCompensationListener.class);
    private final DroneController controller;

    // Injecting the controller to physically manage active drones
    public DroneCompensationListener(DroneController controller) {
        this.controller = controller;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "drone_compensation_queue", durable = "true"),
            exchange = @Exchange(value = RabbitMqConfig.SAGA_EVENTS_EXCHANGE, type = "topic"),
            key = "saga.compensate.drone"
    ))
    public void handleCompensateDrone(CompensateDroneEvent event) {
        logger.warn("❌ Starting Drone compensation {} for SAGA {}. Reason: {}",
                event.getDroneId(), event.getSagaId(), event.getReason());

        try {
            logger.info("🚁 ABORT MISSION signal sent to drone {}...", event.getDroneId());

            // Real compensation logic: detach the drone from the order
            controller.detachDrone(event.getDroneId());

            logger.info("🔋 Drone {} detached from the order and returned to the pool.", event.getDroneId());
            logger.info("✅ Drone {} compensation completed successfully.", event.getDroneId());

        } catch (Exception e) {
            logger.error("🚨 Error during drone {} compensation", event.getDroneId(), e);
        }
    }
}