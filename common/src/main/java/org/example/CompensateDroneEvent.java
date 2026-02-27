package org.example;

import java.time.LocalDateTime;

public class CompensateDroneEvent extends SagaEvent {
    private final String droneId;
    private final String reason;

    public CompensateDroneEvent(String sagaId, String orderId, String droneId,
                                String reason, LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.droneId = droneId;
        this.reason = reason;
    }

    public String getDroneId() {
        return droneId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getEventType() {
        return "COMPENSATE_DRONE";
    }
}
