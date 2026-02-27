package org.example;

import java.time.LocalDateTime;

public class DeliveryScheduledEvent extends SagaEvent {
    private final String deliveryId;

    public DeliveryScheduledEvent(String sagaId, String orderId, String deliveryId,
                                  LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.deliveryId = deliveryId;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    @Override
    public String getEventType() {
        return "DELIVERY_SCHEDULED";
    }
}
