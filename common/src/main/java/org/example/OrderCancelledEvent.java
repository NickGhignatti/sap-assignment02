package org.example;

import java.time.LocalDateTime;

public class OrderCancelledEvent extends SagaEvent {
    private final String reason;

    public OrderCancelledEvent(String sagaId, String orderId, String reason,
                               LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getEventType() {
        return "ORDER_CANCELLED";
    }
}
