package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for drone operations with Event Sourcing support.
 * Provides endpoints to query current state and historical events.
 */
@RestController
@RequestMapping("/")
public class DroneController {
    private final HashMap<String, Drone> dispatchedDrones = new HashMap<>();
    private final DroneEventStore eventStore;

    public DroneController(DroneEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void attachDrone(final String droneId, final Drone drone) {
        this.dispatchedDrones.put(droneId, drone);
    }

    public void detachDrone(final String droneId) {
        this.dispatchedDrones.remove(droneId);
    }

    public HashMap<String, Drone> getCurrentDispatchedDrones() {
        return this.dispatchedDrones;
    }

    /**
     * Get current drone status for an order
     */
    @GetMapping
    public ResponseEntity<String> getOrderStatus(@RequestBody DroneRequest request) {
        if (dispatchedDrones.containsKey(request.orderId())) {
            return ResponseEntity.ok(dispatchedDrones.get(request.orderId()).toString());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get event history for a specific drone
     */
    @GetMapping("/drone/{droneId}/events")
    public ResponseEntity<List<EventSummary>> getDroneEventHistory(@PathVariable String droneId) {
        List<DroneEvent> events = eventStore.getEventsForDrone(droneId);

        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<EventSummary> summaries = events.stream()
                .map(e -> new EventSummary(
                        e.getEventType(),
                        e.getTimestamp().toString(),
                        e.getVersion()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(summaries);
    }

    /**
     * Get event history for a specific order
     */
    @GetMapping("/order/{orderId}/events")
    public ResponseEntity<List<EventSummary>> getOrderEventHistory(@PathVariable String orderId) {
        List<DroneEvent> events = eventStore.getEventsForOrder(orderId);

        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<EventSummary> summaries = events.stream()
                .map(e -> new EventSummary(
                        e.getEventType(),
                        e.getTimestamp().toString(),
                        e.getVersion()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(summaries);
    }

    /**
     * Rebuild drone state from event history
     */
    @GetMapping("/drone/{droneId}/rebuild")
    public ResponseEntity<String> rebuildDroneState(@PathVariable String droneId) {
        Drone drone = eventStore.rebuildDroneFromEvents(droneId);

        if (drone == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(drone.toString());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Drone service is running");
    }
}

/**
 * DTO for event summary responses
 */
record EventSummary(String eventType, String timestamp, long version) {
}