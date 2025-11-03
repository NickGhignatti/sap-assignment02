package org.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Shared message class for order information.
 * This class will be used across all microservices (customer, delivery, drone).
 */
public record OrderMessage(String orderId, String customerId, String fromAddress, String toAddress,
                           double packageWeight, LocalDateTime requestedDeliveryTime, int maxDeliveryTimeMinutes) {
    @JsonCreator
    public OrderMessage(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("customerId") String customerId,
            @JsonProperty("fromAddress") String fromAddress,
            @JsonProperty("toAddress") String toAddress,
            @JsonProperty("packageWeight") double packageWeight,
            @JsonProperty("requestedDeliveryTime") LocalDateTime requestedDeliveryTime,
            @JsonProperty("maxDeliveryTimeMinutes") int maxDeliveryTimeMinutes) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.packageWeight = packageWeight;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.maxDeliveryTimeMinutes = maxDeliveryTimeMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderMessage that = (OrderMessage) o;
        return Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "OrderMessage{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", from='" + fromAddress + '\'' +
                ", to='" + toAddress + '\'' +
                ", weight=" + packageWeight +
                ", requestedTime=" + requestedDeliveryTime +
                ", maxTime=" + maxDeliveryTimeMinutes +
                '}';
    }
}