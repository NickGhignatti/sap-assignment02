package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Event Store service implementing the Event Sourcing pattern.
 * Stores all drone state changes as immutable events in MongoDB.
 */
@Service
public class DroneEventStore {
    private static final Logger logger = LoggerFactory.getLogger(DroneEventStore.class);
    private final DroneEventRepository repository;
    private final ObjectMapper objectMapper;

    public DroneEventStore(DroneEventRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Save a new event to the event store
     */
    public void saveEvent(DroneEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);

            DroneEventDocument document = new DroneEventDocument(
                    event.getDroneId(),
                    event.getOrderId(),
                    event.getEventType(),
                    event.getTimestamp(),
                    event.getVersion(),
                    eventData
            );

            repository.save(document);
            logger.info("Saved event: {} for drone: {}",
                    event.getEventType(), event.getDroneId());
        } catch (Exception e) {
            logger.error("Failed to save event", e);
            throw new RuntimeException("Failed to save event", e);
        }
    }

    /**
     * Load all events for a specific drone
     */
    public List<DroneEvent> getEventsForDrone(String droneId) {
        List<DroneEventDocument> documents = repository.findByDroneIdOrderByTimestampAsc(droneId);
        List<DroneEvent> events = new ArrayList<>();

        for (DroneEventDocument doc : documents) {
            try {
                DroneEvent event = objectMapper.readValue(doc.getEventData(), DroneEvent.class);
                events.add(event);
            } catch (Exception e) {
                logger.error("Failed to deserialize event: {}", doc.getId(), e);
            }
        }

        return events;
    }

    /**
     * Load all events for a specific order
     */
    public List<DroneEvent> getEventsForOrder(String orderId) {
        List<DroneEventDocument> documents = repository.findByOrderIdOrderByTimestampAsc(orderId);
        List<DroneEvent> events = new ArrayList<>();

        for (DroneEventDocument doc : documents) {
            try {
                DroneEvent event = objectMapper.readValue(doc.getEventData(), DroneEvent.class);
                events.add(event);
            } catch (Exception e) {
                logger.error("Failed to deserialize event: {}", doc.getId(), e);
            }
        }

        return events;
    }

    /**
     * Get the current version (event count) for a drone
     */
    public long getCurrentVersion(String droneId) {
        return repository.countByDroneId(droneId);
    }

    /**
     * Rebuild drone state from events
     */
    public Drone rebuildDroneFromEvents(String droneId) {
        List<DroneEvent> events = getEventsForDrone(droneId);

        if (events.isEmpty()) {
            return null;
        }

        // Start with first event (DRONE_CREATED)
        DroneCreatedEvent createdEvent = (DroneCreatedEvent) events.get(0);
        OrderMessage order = new OrderMessage(
                createdEvent.getOrderId(),
                "reconstructed", // customerId not stored in event
                createdEvent.getFromAddress(),
                createdEvent.getToAddress(),
                createdEvent.getPackageWeight(),
                createdEvent.getRequestedDeliveryTime(),
                createdEvent.getMaxDeliveryTimeMinutes()
        );

        Drone drone = new Drone(order, createdEvent.getDroneId());

        // Apply subsequent events
        for (int i = 1; i < events.size(); i++) {
            DroneEvent event = events.get(i);

            if (event instanceof DroneDispatchedEvent) {
                drone.start();
            } else if (event instanceof DroneDeliveredEvent) {
                drone.end();
            } else if (event instanceof DroneReturnedEvent) {
                // Drone is already in returning state from end()
            }
        }

        return drone;
    }
}