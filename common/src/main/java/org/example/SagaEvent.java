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

    public String getSagaId() { return sagaId; }
    public String getOrderId() { return orderId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public abstract String getEventType();
}

// ============================================================================
// SUCCESS EVENTS
// ============================================================================

class OrderSagaStartedEvent extends SagaEvent {
    private final String customerId;
    private final String fromAddress;
    private final String toAddress;
    private final double packageWeight;
    private final LocalDateTime requestedDeliveryTime;
    private final int maxDeliveryTimeMinutes;

    public OrderSagaStartedEvent(String sagaId, String orderId, String customerId,
                                 String fromAddress, String toAddress, double packageWeight,
                                 LocalDateTime requestedDeliveryTime, int maxDeliveryTimeMinutes,
                                 LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.customerId = customerId;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.packageWeight = packageWeight;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.maxDeliveryTimeMinutes = maxDeliveryTimeMinutes;
    }

    public String getCustomerId() { return customerId; }
    public String getFromAddress() { return fromAddress; }
    public String getToAddress() { return toAddress; }
    public double getPackageWeight() { return packageWeight; }
    public LocalDateTime getRequestedDeliveryTime() { return requestedDeliveryTime; }
    public int getMaxDeliveryTimeMinutes() { return maxDeliveryTimeMinutes; }

    @Override
    public String getEventType() { return "ORDER_SAGA_STARTED"; }
}

class OrderValidatedEvent extends SagaEvent {
    public OrderValidatedEvent(String sagaId, String orderId, LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
    }

    @Override
    public String getEventType() { return "ORDER_VALIDATED"; }
}

class DeliveryScheduledEvent extends SagaEvent {
    private final String deliveryId;

    public DeliveryScheduledEvent(String sagaId, String orderId, String deliveryId,
                                  LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.deliveryId = deliveryId;
    }

    public String getDeliveryId() { return deliveryId; }

    @Override
    public String getEventType() { return "DELIVERY_SCHEDULED"; }
}

class DroneAssignedEvent extends SagaEvent {
    private final String droneId;

    public DroneAssignedEvent(String sagaId, String orderId, String droneId,
                              LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.droneId = droneId;
    }

    public String getDroneId() { return droneId; }

    @Override
    public String getEventType() { return "DRONE_ASSIGNED"; }
}

class OrderCompletedEvent extends SagaEvent {
    public OrderCompletedEvent(String sagaId, String orderId, LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
    }

    @Override
    public String getEventType() { return "ORDER_COMPLETED"; }
}

// ============================================================================
// FAILURE EVENTS
// ============================================================================

class OrderValidationFailedEvent extends SagaEvent {
    private final String reason;

    public OrderValidationFailedEvent(String sagaId, String orderId, String reason,
                                      LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.reason = reason;
    }

    public String getReason() { return reason; }

    @Override
    public String getEventType() { return "ORDER_VALIDATION_FAILED"; }
}

class DeliverySchedulingFailedEvent extends SagaEvent {
    private final String reason;

    public DeliverySchedulingFailedEvent(String sagaId, String orderId, String reason,
                                         LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.reason = reason;
    }

    public String getReason() { return reason; }

    @Override
    public String getEventType() { return "DELIVERY_SCHEDULING_FAILED"; }
}

class DroneAssignmentFailedEvent extends SagaEvent {
    private final String reason;

    public DroneAssignmentFailedEvent(String sagaId, String orderId, String reason,
                                      LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.reason = reason;
    }

    public String getReason() { return reason; }

    @Override
    public String getEventType() { return "DRONE_ASSIGNMENT_FAILED"; }
}

// ============================================================================
// COMPENSATION EVENTS (for rollback)
// ============================================================================

class CompensateOrderEvent extends SagaEvent {
    private final String reason;

    public CompensateOrderEvent(String sagaId, String orderId, String reason,
                                LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.reason = reason;
    }

    public String getReason() { return reason; }

    @Override
    public String getEventType() { return "COMPENSATE_ORDER"; }
}

class CompensateDeliveryEvent extends SagaEvent {
    private final String deliveryId;
    private final String reason;

    public CompensateDeliveryEvent(String sagaId, String orderId, String deliveryId,
                                   String reason, LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.deliveryId = deliveryId;
        this.reason = reason;
    }

    public String getDeliveryId() { return deliveryId; }
    public String getReason() { return reason; }

    @Override
    public String getEventType() { return "COMPENSATE_DELIVERY"; }
}

class CompensateDroneEvent extends SagaEvent {
    private final String droneId;
    private final String reason;

    public CompensateDroneEvent(String sagaId, String orderId, String droneId,
                                String reason, LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.droneId = droneId;
        this.reason = reason;
    }

    public String getDroneId() { return droneId; }
    public String getReason() { return reason; }

    @Override
    public String getEventType() { return "COMPENSATE_DRONE"; }
}

class OrderCancelledEvent extends SagaEvent {
    private final String reason;

    public OrderCancelledEvent(String sagaId, String orderId, String reason,
                               LocalDateTime timestamp) {
        super(sagaId, orderId, timestamp);
        this.reason = reason;
    }

    public String getReason() { return reason; }

    @Override
    public String getEventType() { return "ORDER_CANCELLED"; }
}