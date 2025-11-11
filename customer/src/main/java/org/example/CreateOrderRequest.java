package org.example;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CreateOrderRequest(String customerId, String fromAddress, String toAddress, double packageWeight,
                                 @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime requestedDeliveryTime,
                                 int maxDeliveryTimeMinutes) {
    public CreateOrderRequest(String customerId, String fromAddress, String toAddress,
                              double packageWeight, LocalDateTime requestedDeliveryTime,
                              int maxDeliveryTimeMinutes) {
        this.customerId = customerId;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.packageWeight = packageWeight;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.maxDeliveryTimeMinutes = maxDeliveryTimeMinutes;
    }

    @Override
    public LocalDateTime requestedDeliveryTime() {
        return requestedDeliveryTime;
    }
}