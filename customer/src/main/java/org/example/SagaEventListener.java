package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Listens for SAGA compensation events and executes rollback actions.
 * Each service should have its own compensation logic.
 */
@Service
public class SagaEventListener {
    private static final Logger logger = LoggerFactory.getLogger(SagaEventListener.class);

    /**
     * Handle order compensation
     */
    @RabbitListener(queues = RabbitMqConfig.SAGA_COMPENSATION_QUEUE)
    public void handleCompensateOrder(CompensateOrderEvent event) {
        logger.info("Compensating order {} for SAGA {}: {}",
                event.getOrderId(), event.getSagaId(), event.getReason());

        try {
            // Compensation logic:
            // - Mark order as cancelled
            // - Refund customer (if payment was processed)
            // - Send cancellation notification
            // - Update inventory

            logger.info("Order {} compensation completed", event.getOrderId());

        } catch (Exception e) {
            logger.error("Failed to compensate order {}", event.getOrderId(), e);
            // In production: retry logic, dead letter queue, alerting
        }
    }

    /**
     * Handle delivery compensation
     */
    @RabbitListener(queues = RabbitMqConfig.SAGA_COMPENSATION_QUEUE)
    public void handleCompensateDelivery(CompensateDeliveryEvent event) {
        logger.info("Compensating delivery {} for SAGA {}: {}",
                event.getDeliveryId(), event.getSagaId(), event.getReason());

        try {
            // Compensation logic:
            // - Cancel delivery schedule
            // - Release delivery slot
            // - Notify delivery service

            logger.info("Delivery {} compensation completed", event.getDeliveryId());

        } catch (Exception e) {
            logger.error("Failed to compensate delivery {}", event.getDeliveryId(), e);
        }
    }

    /**
     * Handle drone compensation
     */
    @RabbitListener(queues = RabbitMqConfig.SAGA_COMPENSATION_QUEUE)
    public void handleCompensateDrone(CompensateDroneEvent event) {
        logger.info("Compensating drone {} for SAGA {}: {}",
                event.getDroneId(), event.getSagaId(), event.getReason());

        try {
            // Compensation logic:
            // - Cancel drone assignment
            // - Return drone to available pool
            // - Clear drone schedule

            logger.info("Drone {} compensation completed", event.getDroneId());

        } catch (Exception e) {
            logger.error("Failed to compensate drone {}", event.getDroneId(), e);
        }
    }

    /**
     * Log SAGA events for monitoring
     */
    @RabbitListener(queues = RabbitMqConfig.SAGA_EVENTS_QUEUE)
    public void logSagaEvent(SagaEvent event) {
        logger.info("SAGA Event: {} - {} for order {}",
                event.getEventType(), event.getSagaId(), event.getOrderId());
    }
}