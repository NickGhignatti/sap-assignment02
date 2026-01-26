package org.example;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

/**
 * Base class for all drone events in the event sourcing pattern.
 * Each event represents a state change in the drone's lifecycle.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DroneCreatedEvent.class, name = "DRONE_CREATED"),
        @JsonSubTypes.Type(value = DroneDispatchedEvent.class, name = "DRONE_DISPATCHED"),
        @JsonSubTypes.Type(value = DroneDeliveredEvent.class, name = "DRONE_DELIVERED"),
        @JsonSubTypes.Type(value = DroneReturnedEvent.class, name = "DRONE_RETURNED")
})
public abstract class DroneEvent {
    private final String droneId;
    private final String orderId;
    private final LocalDateTime timestamp;
    private final long version;

    protected DroneEvent(String droneId, String orderId, LocalDateTime timestamp, long version) {
        this.droneId = droneId;
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.version = version;
    }

    public String getDroneId() {
        return droneId;
    }

    public String getOrderId() {
        return orderId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public long getVersion() {
        return version;
    }

    public abstract String getEventType();
}

/**
 * Event fired when a new drone is created for an order
 */
class DroneCreatedEvent extends DroneEvent {
    private final String fromAddress;
    private final String toAddress;
    private final double packageWeight;
    private final LocalDateTime requestedDeliveryTime;
    private final int maxDeliveryTimeMinutes;

    public DroneCreatedEvent(String droneId, String orderId, String fromAddress,
                             String toAddress, double packageWeight,
                             LocalDateTime requestedDeliveryTime, int maxDeliveryTimeMinutes,
                             LocalDateTime timestamp, long version) {
        super(droneId, orderId, timestamp, version);
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.packageWeight = packageWeight;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.maxDeliveryTimeMinutes = maxDeliveryTimeMinutes;
    }

    public String getFromAddress() { return fromAddress; }
    public String getToAddress() { return toAddress; }
    public double getPackageWeight() { return packageWeight; }
    public LocalDateTime getRequestedDeliveryTime() { return requestedDeliveryTime; }
    public int getMaxDeliveryTimeMinutes() { return maxDeliveryTimeMinutes; }

    @Override
    public String getEventType() {
        return "DRONE_CREATED";
    }
}

/**
 * Event fired when a drone is dispatched for delivery
 */
class DroneDispatchedEvent extends DroneEvent {
    private final LocalDateTime dispatchTime;

    public DroneDispatchedEvent(String droneId, String orderId,
                                LocalDateTime dispatchTime,
                                LocalDateTime timestamp, long version) {
        super(droneId, orderId, timestamp, version);
        this.dispatchTime = dispatchTime;
    }

    public LocalDateTime getDispatchTime() {
        return dispatchTime;
    }

    @Override
    public String getEventType() {
        return "DRONE_DISPATCHED";
    }
}

/**
 * Event fired when a drone completes delivery
 */
class DroneDeliveredEvent extends DroneEvent {
    private final LocalDateTime deliveryTime;

    public DroneDeliveredEvent(String droneId, String orderId,
                               LocalDateTime deliveryTime,
                               LocalDateTime timestamp, long version) {
        super(droneId, orderId, timestamp, version);
        this.deliveryTime = deliveryTime;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    @Override
    public String getEventType() {
        return "DRONE_DELIVERED";
    }
}

/**
 * Event fired when a drone returns to base
 */
class DroneReturnedEvent extends DroneEvent {
    private final LocalDateTime returnTime;

    public DroneReturnedEvent(String droneId, String orderId,
                              LocalDateTime returnTime,
                              LocalDateTime timestamp, long version) {
        super(droneId, orderId, timestamp, version);
        this.returnTime = returnTime;
    }

    public LocalDateTime getReturnTime() {
        return returnTime;
    }

    @Override
    public String getEventType() {
        return "DRONE_RETURNED";
    }
}