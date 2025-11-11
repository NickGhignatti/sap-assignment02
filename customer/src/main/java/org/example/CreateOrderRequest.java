package org.example;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class CreateOrderRequest {
    private String customerId;
    private String fromAddress;
    private String toAddress;
    private double packageWeight;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedDeliveryTime;
    private int maxDeliveryTimeMinutes;

    public CreateOrderRequest() {
    }

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
}