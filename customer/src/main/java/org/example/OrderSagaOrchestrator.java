package org.example;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orchestrator for the Order SAGA pattern.
 *
 * SAGA Flow (Asynchronous):
 * 1. Validate Order (Customer Service)
 * 2. Schedule Delivery (Delivery Service)
 * 3. Assign Drone (Drone Service)
 *
 * If any step fails, compensating transactions are executed in reverse order.
 */
@Service
public class OrderSagaOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(OrderSagaOrchestrator.class);

    private final OrderSagaRepository sagaRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Counter sagaStartedCounter;
    private final Counter sagaCompletedCounter;
    private final Counter sagaFailedCounter;
    private final Counter sagaCompensatedCounter;

    public OrderSagaOrchestrator(OrderSagaRepository sagaRepository,
                                 RabbitTemplate rabbitTemplate,
                                 MeterRegistry registry) {
        this.sagaRepository = sagaRepository;
        this.rabbitTemplate = rabbitTemplate;

        this.sagaStartedCounter = Counter.builder("saga_started_total")
                .description("Total SAGAs started")
                .register(registry);
        this.sagaCompletedCounter = Counter.builder("saga_completed_total")
                .description("Total SAGAs completed successfully")
                .register(registry);
        this.sagaFailedCounter = Counter.builder("saga_failed_total")
                .description("Total SAGAs failed")
                .register(registry);
        this.sagaCompensatedCounter = Counter.builder("saga_compensated_total")
                .description("Total SAGAs compensated (rolled back)")
                .register(registry);
    }

    /**
     * Start a new order SAGA
     */
    public String startOrderSaga(String orderId, String customerId, String fromAddress,
                                 String toAddress, double packageWeight,
                                 LocalDateTime requestedDeliveryTime, int maxDeliveryTimeMinutes) {

        String sagaId = UUID.randomUUID().toString();

        // Create SAGA state
        OrderSagaState saga = new OrderSagaState(
                sagaId, orderId, customerId, fromAddress, toAddress,
                packageWeight, requestedDeliveryTime, maxDeliveryTimeMinutes
        );

        sagaRepository.save(saga);
        sagaStartedCounter.increment();

        logger.info("Started SAGA {} for order {}", sagaId, orderId);

        // Publish SAGA started event
        OrderSagaStartedEvent event = new OrderSagaStartedEvent(
                sagaId, orderId, customerId, fromAddress, toAddress,
                packageWeight, requestedDeliveryTime, maxDeliveryTimeMinutes,
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                "saga.started", event);

        // Start first step: Order Validation
        validateOrder(saga);

        return sagaId;
    }

    /**
     * Step 1: Validate order
     */
    private void validateOrder(OrderSagaState saga) {
        logger.info("SAGA {}: Validating order {}", saga.getSagaId(), saga.getOrderId());

        try {
            // Simulate validation logic
            if (saga.getPackageWeight() <= 0) {
                handleValidationFailure(saga, "Invalid package weight");
                return;
            }

            if (saga.getFromAddress() == null || saga.getToAddress() == null) {
                handleValidationFailure(saga, "Missing addresses");
                return;
            }

            // Validation successful
            saga.markStepCompleted(SagaStep.ORDER_VALIDATION);
            saga.moveToNextStep();
            sagaRepository.save(saga);

            // Publish success event
            OrderValidatedEvent event = new OrderValidatedEvent(
                    saga.getSagaId(), saga.getOrderId(), LocalDateTime.now()
            );
            rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                    "saga.validated", event);

            logger.info("SAGA {}: Order validated successfully", saga.getSagaId());

            // Initiate Step 2 (Delivery Scheduling) by sending the order to the Delivery Service
            OrderMessage deliveryRequest = new OrderMessage(
                    saga.getOrderId(), saga.getCustomerId(), saga.getFromAddress(),
                    saga.getToAddress(), saga.getPackageWeight(),
                    saga.getRequestedDeliveryTime(), saga.getMaxDeliveryTimeMinutes()
            );

            rabbitTemplate.convertAndSend(RabbitMqConfig.ORDER_QUEUE, deliveryRequest);
            logger.info("SAGA {}: Order sent to the Delivery Service. Waiting for response...", saga.getSagaId());

            // The orchestrator stops here and waits for asynchronous events.

        } catch (Exception e) {
            logger.error("SAGA {}: Order validation failed", saga.getSagaId(), e);
            handleValidationFailure(saga, e.getMessage());
        }
    }

    /**
     * UNIFIED ASYNCHRONOUS LISTENER: Reacts to events published by other microservices.
     */
    @RabbitListener(queues = RabbitMqConfig.SAGA_EVENTS_QUEUE)
    public void handleSagaEvents(SagaEvent event) {

        // Generic Log
        logger.info("SAGA Event Received: {} for order {}", event.getEventType(), event.getOrderId());

        if (event instanceof DeliveryScheduledEvent) {
            DeliveryScheduledEvent deliveryEvent = (DeliveryScheduledEvent) event;

            OrderSagaState saga = sagaRepository.findByOrderId(deliveryEvent.getOrderId()).orElse(null);
            if (saga != null) {
                saga.setDeliveryId(deliveryEvent.getDeliveryId());
                saga.markStepCompleted(SagaStep.DELIVERY_SCHEDULING);
                saga.moveToNextStep(); // Moves to DRONE_ASSIGNMENT
                sagaRepository.save(saga);
                logger.info("SAGA {}: Delivery planned notification received (DeliveryID: {})",
                        saga.getSagaId(), deliveryEvent.getDeliveryId());
            }

        } else if (event instanceof DroneAssignedEvent) {
            DroneAssignedEvent droneEvent = (DroneAssignedEvent) event;

            OrderSagaState saga = sagaRepository.findByOrderId(droneEvent.getOrderId()).orElse(null);
            if (saga != null) {
                saga.setDroneId(droneEvent.getDroneId());
                saga.markStepCompleted(SagaStep.DRONE_ASSIGNMENT);
                sagaRepository.save(saga);
                logger.info("SAGA {}: Drone assigned notification received (DroneID: {})",
                        saga.getSagaId(), droneEvent.getDroneId());

                // Trigger SAGA Completion
                completeOrderSaga(saga);
            }
        }
        // Other events (like OrderSagaStartedEvent) are ignored by the orchestrator
        // as they don't trigger state machine transitions.
    }

    /**
     * Complete the SAGA successfully
     */
    private void completeOrderSaga(OrderSagaState saga) {
        saga.setStatus(SagaStatus.COMPLETED);
        saga.setEndTime(LocalDateTime.now());
        sagaRepository.save(saga);
        sagaCompletedCounter.increment();

        OrderCompletedEvent event = new OrderCompletedEvent(
                saga.getSagaId(), saga.getOrderId(), LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                "saga.completed", event);

        logger.info("SAGA {}: Completed successfully for order {}",
                saga.getSagaId(), saga.getOrderId());
    }

    // ========================================================================
    // FAILURE HANDLERS - Trigger compensation
    // ========================================================================

    private void handleValidationFailure(OrderSagaState saga, String reason) {
        saga.markFailed(reason);
        sagaRepository.save(saga);
        sagaFailedCounter.increment();

        OrderValidationFailedEvent event = new OrderValidationFailedEvent(
                saga.getSagaId(), saga.getOrderId(), reason, LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                "saga.validation.failed", event);

        logger.error("SAGA {}: Failed at validation - {}", saga.getSagaId(), reason);

        // No compensation needed as no steps were completed
        cancelOrder(saga, reason);
    }

    private void handleDeliveryFailure(OrderSagaState saga, String reason) {
        saga.markFailed(reason);
        sagaRepository.save(saga);
        sagaFailedCounter.increment();

        DeliverySchedulingFailedEvent event = new DeliverySchedulingFailedEvent(
                saga.getSagaId(), saga.getOrderId(), reason, LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                "saga.delivery.failed", event);

        logger.error("SAGA {}: Failed at delivery scheduling - {}",
                saga.getSagaId(), reason);

        // Compensate: Cancel order validation
        compensateSaga(saga);
    }

    private void handleDroneFailure(OrderSagaState saga, String reason) {
        saga.markFailed(reason);
        sagaRepository.save(saga);
        sagaFailedCounter.increment();

        DroneAssignmentFailedEvent event = new DroneAssignmentFailedEvent(
                saga.getSagaId(), saga.getOrderId(), reason, LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                "saga.drone.failed", event);

        logger.error("SAGA {}: Failed at drone assignment - {}",
                saga.getSagaId(), reason);

        // Compensate: Cancel delivery and order
        compensateSaga(saga);
    }

    // ========================================================================
    // COMPENSATION (Rollback)
    // ========================================================================

    /**
     * Execute compensating transactions in reverse order
     */
    private void compensateSaga(OrderSagaState saga) {
        logger.info("SAGA {}: Starting compensation", saga.getSagaId());
        saga.startCompensation();
        sagaRepository.save(saga);

        // Get steps to compensate in reverse order
        for (SagaStep step : saga.getStepsToCompensate()) {
            switch (step) {
                case DRONE_ASSIGNMENT -> compensateDrone(saga);
                case DELIVERY_SCHEDULING -> compensateDelivery(saga);
                case ORDER_VALIDATION -> compensateOrder(saga);
            }
        }

        // Mark SAGA as compensated
        saga.markCompensated();
        sagaRepository.save(saga);
        sagaCompensatedCounter.increment();

        logger.info("SAGA {}: Compensation completed", saga.getSagaId());

        // Cancel the order
        cancelOrder(saga, saga.getFailureReason());
    }

    private void compensateOrder(OrderSagaState saga) {
        logger.info("SAGA {}: Compensating order validation", saga.getSagaId());

        CompensateOrderEvent event = new CompensateOrderEvent(
                saga.getSagaId(), saga.getOrderId(),
                saga.getFailureReason(), LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                "saga.compensate.order", event);
    }

    private void compensateDelivery(OrderSagaState saga) {
        logger.info("SAGA {}: Compensating delivery scheduling", saga.getSagaId());

        CompensateDeliveryEvent event = new CompensateDeliveryEvent(
                saga.getSagaId(), saga.getOrderId(), saga.getDeliveryId(),
                saga.getFailureReason(), LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                "saga.compensate.delivery", event);
    }

    private void compensateDrone(OrderSagaState saga) {
        logger.info("SAGA {}: Compensating drone assignment", saga.getSagaId());

        CompensateDroneEvent event = new CompensateDroneEvent(
                saga.getSagaId(), saga.getOrderId(), saga.getDroneId(),
                saga.getFailureReason(), LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                "saga.compensate.drone", event);
    }

    private void cancelOrder(OrderSagaState saga, String reason) {
        logger.info("SAGA {}: Cancelling order {}", saga.getSagaId(), saga.getOrderId());

        OrderCancelledEvent event = new OrderCancelledEvent(
                saga.getSagaId(), saga.getOrderId(), reason, LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE,
                "saga.cancelled", event);
    }

    /**
     * Get SAGA status
     */
    public OrderSagaState getSagaState(String sagaId) {
        return sagaRepository.findById(sagaId).orElse(null);
    }

    /**
     * Get SAGA by order ID
     */
    public OrderSagaState getSagaByOrderId(String orderId) {
        return sagaRepository.findByOrderId(orderId).orElse(null);
    }
}