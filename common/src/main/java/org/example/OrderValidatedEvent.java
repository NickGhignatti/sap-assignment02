package org.example;

import java.time.LocalDateTime;

public class OrderValidatedEvent extends SagaEvent {
    public OrderValidatedEvent(String sagaId, String orderId, LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
    }

    @Override
    public String getEventType() {
        return "ORDER_VALIDATED";
    }
}
