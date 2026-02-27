package org.example;

import java.time.LocalDateTime;

public class CompensateOrderEvent extends SagaEvent {
    private final String reason;

    public CompensateOrderEvent(String sagaId, String orderId, String reason,
                                LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getEventType() {
        return "COMPENSATE_ORDER";
    }
}
