package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service that handles drone operations using Event Sourcing pattern.
 * All state changes are recorded as events in the event store.
 */
@Service
public class DroneService {
    private static final Logger logger = LoggerFactory.getLogger(DroneService.class);
    private final DroneEventStore eventStore;
    private final DroneController controller;

    public DroneService(DroneEventStore eventStore, DroneController controller) {
        this.eventStore = eventStore;
        this.controller = controller;
    }

    /**
     * Create a new drone for an order and store the creation event
     */
    public Drone createDrone(OrderMessage order) {
        Drone drone = new Drone(order);

        // Create and store the DRONE_CREATED event
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
     * Process a complete drone delivery lifecycle with event sourcing
     */
    public void processDroneDelivery(OrderMessage order, int sleepMinutes) {
        // Create drone and record event
        Drone drone = createDrone(order);

        // Dispatch drone and record event
        drone.start();
        dispatchDrone(drone.getId(), order.orderId());
        controller.attachDrone(order.orderId(), drone);

        try {
            // Simulate delivery time
            Thread.sleep(sleepMinutes * 60_000L);

            // Complete delivery and record events
            drone.end();
            completeDroneDelivery(drone.getId(), order.orderId());
            recordDroneReturn(drone.getId(), order.orderId());

            controller.detachDrone(drone.getId());
        } catch (InterruptedException e) {
            logger.error("Delivery interrupted for drone {}", drone.getId(), e);
            Thread.currentThread().interrupt();
        }
    }
}