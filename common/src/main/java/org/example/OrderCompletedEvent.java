package org.example;

import java.time.LocalDateTime;

public class OrderCompletedEvent extends SagaEvent {
    public OrderCompletedEvent(String sagaId, String orderId, LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
    }

    @Override
    public String getEventType() {
        return "ORDER_COMPLETED";
    }
}
