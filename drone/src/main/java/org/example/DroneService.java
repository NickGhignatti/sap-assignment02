package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service that handles drone operations using Event Sourcing pattern.
 * All state changes are recorded as events in the event store.
 */
@Service
public class DroneService {
    private final RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DroneService.class);
    private final DroneEventStore eventStore;
    private final DroneController controller;

    public DroneService(RabbitTemplate rabbitTemplate, DroneEventStore eventStore, DroneController controller) {
        this.eventStore = eventStore;
        this.controller = controller;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Create a new drone for an order and store the creation event
     */
    public Drone createDrone(OrderMessage order) {
        Drone drone = new Drone(order);

        // Create and store the DRONE_CREATED event (for Event Sourcing)
        DroneCreatedEvent event = new DroneCreatedEvent(
                drone.getId(),
                order.orderId(),
                order.fromAddress(),
                order.toAddress(),
                order.packageWeight(),
                order.requestedDeliveryTime(),
                order.maxDeliveryTimeMinutes(),
                LocalDateTime.now(),
                0 // First event is version 0
        );

        eventStore.saveEvent(event);
        logger.info("Created drone {} for order {}", drone.getId(), order.orderId());

        DroneAssignedEvent sagaEvent = new DroneAssignedEvent(
                "unknown", order.orderId(), drone.getId(), LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.SAGA_EVENTS_EXCHANGE, "saga.drone_assigned", sagaEvent);

        return drone;
    }

    /**
     * Dispatch a drone and record the event
     */
    public void dispatchDrone(String droneId, String orderId) {
        long version = eventStore.getCurrentVersion(droneId);

        DroneDispatchedEvent event = new DroneDispatchedEvent(
                droneId,
                orderId,
                LocalDateTime.now(),
                LocalDateTime.now(),
                version
        );

        eventStore.saveEvent(event);
        logger.info("Dispatched drone {} for order {}", droneId, orderId);
    }

    /**
     * Mark drone delivery as complete and record the event
     */
    public void completeDroneDelivery(String droneId, String orderId) {
        long version = eventStore.getCurrentVersion(droneId);

        DroneDeliveredEvent event = new DroneDeliveredEvent(
                droneId,
                orderId,
                LocalDateTime.now(),
                LocalDateTime.now(),
                version
        );

        eventStore.saveEvent(event);
        logger.info("Drone {} delivered order {}", droneId, orderId);
    }

    /**
     * Record drone return and record the event
     */
    public void recordDroneReturn(String droneId, String orderId) {
        long version = eventStore.getCurrentVersion(droneId);

        DroneReturnedEvent event = new DroneReturnedEvent(
                droneId,
                orderId,
                LocalDateTime.now(),
                LocalDateTime.now(),
                version
        );

        eventStore.saveEvent(event);
        logger.info("Drone {} returned from order {}", droneId, orderId);
    }

    /**
     * Rebuild a drone's state from its event history
     */
    public Drone getDroneFromHistory(String droneId) {
        return eventStore.rebuildDroneFromEvents(droneId);
    }

    /**
     * Process a drone delivery asynchronously (Replaces the old sleep logic)
     */
    public void startDroneDelivery(OrderMessage order, int sleepMinutes) {
        // Create drone (this also sends the SAGA assigned event)
        Drone drone = createDrone(order);
        drone.start();

        drone.setExpectedArrivalTime(LocalDateTime.now().plusMinutes(sleepMinutes));

        dispatchDrone(drone.getId(), order.orderId());
        controller.attachDrone(order.orderId(), drone);
        logger.info("Drone {} left. It will be to you in {} minutes", drone.getId(), sleepMinutes);
    }

    /**
     * Scheduler that checks periodically which drones have arrived
     */
    @Scheduled(fixedRate = 10000)
    public void checkArrivedDrones() {
        LocalDateTime now = LocalDateTime.now();

        for (Drone drone : controller.getCurrentDispatchedDrones().values()) {
            if (drone.getState() == DroneState.InTransit &&
                    drone.getExpectedArrivalTime() != null &&
                    now.isAfter(drone.getExpectedArrivalTime())) {

                logger.info("Drone {} arrived at destination!", drone.getId());

                drone.end();
                completeDroneDelivery(drone.getId(), drone.getOrder().orderId());
                recordDroneReturn(drone.getId(), drone.getOrder().orderId());

                controller.detachDrone(drone.getId());
            }
        }
    }
}