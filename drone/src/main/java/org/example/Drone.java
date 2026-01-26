package org.example;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

enum DroneState {
    Sleeping,
    InTransit,
    Returning
}

/**
 * Drone entity that can be reconstructed from events.
 * This class represents the current state, built from the event history.
 */
public class Drone {
    private final String droneId;
    private final OrderMessage order;
    private final LocalDateTime deliveryStartTime;
    private DroneState state = DroneState.Sleeping;
    private LocalDateTime dispatchTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime returnTime;

    // Constructor for new drones
    public Drone(final OrderMessage order) {
        this.droneId = UUID.randomUUID().toString();
        this.order = order;
        this.deliveryStartTime = LocalDateTime.now();
    }

    // Constructor for rebuilding from events
    public Drone(final OrderMessage order, String droneId) {
        this.droneId = droneId;
        this.order = order;
        this.deliveryStartTime = LocalDateTime.now();
    }

    public String getId() {
        return this.droneId;
    }

    public OrderMessage getOrder() {
        return this.order;
    }

    public DroneState getState() {
        return this.state;
    }

    public LocalDateTime getDeliveryStartTime() {
        return this.deliveryStartTime;
    }

    public LocalDateTime getDispatchTime() {
        return this.dispatchTime;
    }

    public LocalDateTime getDeliveryTime() {
        return this.deliveryTime;
    }

    public LocalDateTime getReturnTime() {
        return this.returnTime;
    }

    public void start() {
        this.state = DroneState.InTransit;
        this.dispatchTime = LocalDateTime.now();
    }

    public void end() {
        this.state = DroneState.Returning;
        this.deliveryTime = LocalDateTime.now();
        this.returnTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        Duration elapsed = Duration.between(this.deliveryStartTime, LocalDateTime.now());
        long nanos = elapsed.toNanos();
        LocalTime timeDisplay = LocalTime.ofNanoOfDay(nanos % 86400000000000L);
        return "Drone " + this.getId() + " " + this.state +
                " from " + order.fromAddress() +
                " to " + order.toAddress() +
                " in " + timeDisplay;
    }
}