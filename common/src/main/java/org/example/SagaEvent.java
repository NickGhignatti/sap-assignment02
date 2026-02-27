package org.example;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

/**
 * Base class for SAGA orchestration events.
 * SAGA pattern coordinates distributed transactions across microservices.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderSagaStartedEvent.class, name = "ORDER_SAGA_STARTED"),
        @JsonSubTypes.Type(value = OrderValidatedEvent.class, name = "ORDER_VALIDATED"),
        @JsonSubTypes.Type(value = OrderValidationFailedEvent.class, name = "ORDER_VALIDATION_FAILED"),
        @JsonSubTypes.Type(value = DeliveryScheduledEvent.class, name = "DELIVERY_SCHEDULED"),
        @JsonSubTypes.Type(value = DeliverySchedulingFailedEvent.class, name = "DELIVERY_SCHEDULING_FAILED"),
        @JsonSubTypes.Type(value = DroneAssignedEvent.class, name = "DRONE_ASSIGNED"),
        @JsonSubTypes.Type(value = DroneAssignmentFailedEvent.class, name = "DRONE_ASSIGNMENT_FAILED"),
        @JsonSubTypes.Type(value = OrderCompletedEvent.class, name = "ORDER_COMPLETED"),
        @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "ORDER_CANCELLED"),
        @JsonSubTypes.Type(value = CompensateOrderEvent.class, name = "COMPENSATE_ORDER"),
        @JsonSubTypes.Type(value = CompensateDeliveryEvent.class, name = "COMPENSATE_DELIVERY"),
        @JsonSubTypes.Type(value = CompensateDroneEvent.class, name = "COMPENSATE_DRONE")
})

public abstract class SagaEvent {
    private final String sagaId;
    private final String orderId;
    private final LocalDateTime timestamp;

    protected SagaEvent(String sagaId, String orderId, LocalDateTime timestamp) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.timestamp = timestamp;
    }

    public String getSagaId() {
        return sagaId;
    }

    public String getOrderId() {
        return orderId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public abstract String getEventType();
}
