package org.example;

import java.time.LocalDateTime;

public class DeliverySchedulingFailedEvent extends SagaEvent {
    private final String reason;

    public DeliverySchedulingFailedEvent(String sagaId, String orderId, String reason,
                                         LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getEventType() {
        return "DELIVERY_SCHEDULING_FAILED";
    }
}
