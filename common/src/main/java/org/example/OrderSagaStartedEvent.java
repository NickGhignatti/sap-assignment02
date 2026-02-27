package org.example;

import java.time.LocalDateTime;

public class OrderSagaStartedEvent extends SagaEvent {
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

    public String getCustomerId() {
        return customerId;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public double getPackageWeight() {
        return packageWeight;
    }

    public LocalDateTime getRequestedDeliveryTime() {
        return requestedDeliveryTime;
    }

    public int getMaxDeliveryTimeMinutes() {
        return maxDeliveryTimeMinutes;
    }

    @Override
    public String getEventType() {
        return "ORDER_SAGA_STARTED";
    }
}
