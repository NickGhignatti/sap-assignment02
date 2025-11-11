package org.example;

import java.util.UUID;

enum DroneState {
    Sleeping,
    InTransit,
    Returning
}

public class Drone {
    private final String droneId;
    private final OrderMessage order;
    private DroneState state = DroneState.Sleeping;

    public Drone(final OrderMessage order) {
        this.droneId = UUID.randomUUID().toString();
        this.order = order;
    }

    public String getId() {
        return this.droneId;
    }

    public void start() {
        this.state = DroneState.InTransit;
    }

    public void end() {
        this.state = DroneState.Returning;
    }

    @Override
    public String toString() {
        return "Drone " + this.getId() + " " + this.state + " from " + order.fromAddress() + " to " + order.toAddress();
    }
}