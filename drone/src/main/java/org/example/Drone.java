package org.example;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.data.mongodb.mapping.Document;
import org.springframework.data.annotation.Id;

enum DroneState {
    Sleeping,
    InTransit,
    Returning
}

@Document(collection = "sourcing")
public class Drone {
    @Id
    private final String droneId;
    private final OrderMessage order;
    private final LocalDateTime deliveryStartTime;
    private DroneState state = DroneState.Sleeping;

    public Drone(final OrderMessage order) {
        this.droneId = UUID.randomUUID().toString();
        this.order = order;
        this.deliveryStartTime = LocalDateTime.now();
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
        Duration elapsed = Duration.between(this.deliveryStartTime, LocalDateTime.now());
        long nanos = elapsed.toNanos();
        LocalTime timeDisplay = LocalTime.ofNanoOfDay(nanos % 86400000000000L);
        return "Drone " + this.getId() + " " + this.state + " from " + order.fromAddress() + " to " + order.toAddress() + " in " + timeDisplay;
    }
}