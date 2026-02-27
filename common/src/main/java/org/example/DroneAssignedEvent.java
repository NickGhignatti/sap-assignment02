package org.example;

import java.time.LocalDateTime;

public class DroneAssignedEvent extends SagaEvent {
    private final String droneId;

    public DroneAssignedEvent(String sagaId, String orderId, String droneId,
                              LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.droneId = droneId;
    }

    public String getDroneId() {
        return droneId;
    }

    @Override
    public String getEventType() {
        return "DRONE_ASSIGNED";
    }
}
