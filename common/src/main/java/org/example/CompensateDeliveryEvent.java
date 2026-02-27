package org.example;

import java.time.LocalDateTime;

public class CompensateDeliveryEvent extends SagaEvent {
    private final String deliveryId;
    private final String reason;

    public CompensateDeliveryEvent(String sagaId, String orderId, String deliveryId,
                                   String reason, LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.deliveryId = deliveryId;
        this.reason = reason;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getEventType() {
        return "COMPENSATE_DELIVERY";
    }
}
