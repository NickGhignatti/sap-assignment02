package org.example;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state of an order SAGA.
 * Tracks progress through the distributed transaction and supports compensation.
 */
@Document(collection = "order_sagas")
public class OrderSagaState {

    @Id
    private String sagaId;
    private String orderId;
    private SagaStatus status;
    private SagaStep currentStep;
    private List<SagaStep> completedSteps;
    private String failureReason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Order details for compensation
    private String customerId;
    private String fromAddress;
    private String toAddress;
    private double packageWeight;
    private LocalDateTime requestedDeliveryTime;
    private int maxDeliveryTimeMinutes;

    // Service IDs for compensation
    private String deliveryId;
    private String droneId;

    public OrderSagaState() {
        this.completedSteps = new ArrayList<>();
        this.status = SagaStatus.STARTED;
        this.currentStep = SagaStep.ORDER_VALIDATION;
        this.startTime = LocalDateTime.now();
    }

    public OrderSagaState(String sagaId, String orderId, String customerId,
                          String fromAddress, String toAddress, double packageWeight,
                          LocalDateTime requestedDeliveryTime, int maxDeliveryTimeMinutes) {
        this();
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.packageWeight = packageWeight;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.maxDeliveryTimeMinutes = maxDeliveryTimeMinutes;
    }

    // State transitions
    public void markStepCompleted(SagaStep step) {
        this.completedSteps.add(step);
    }

    public void moveToNextStep() {
        switch (currentStep) {
            case ORDER_VALIDATION -> currentStep = SagaStep.DELIVERY_SCHEDULING;
            case DELIVERY_SCHEDULING -> currentStep = SagaStep.DRONE_ASSIGNMENT;
            case DRONE_ASSIGNMENT -> {
                currentStep = SagaStep.COMPLETED;
                status = SagaStatus.COMPLETED;
                endTime = LocalDateTime.now();
            }
            default -> {}
        }
    }

    public void markFailed(String reason) {
        this.status = SagaStatus.FAILED;
        this.failureReason = reason;
        this.endTime = LocalDateTime.now();
    }

    public void startCompensation() {
        this.status = SagaStatus.COMPENSATING;
    }

    public void markCompensated() {
        this.status = SagaStatus.COMPENSATED;
        this.endTime = LocalDateTime.now();
    }

    public boolean needsCompensation() {
        return status == SagaStatus.FAILED && !completedSteps.isEmpty();
    }

    public List<SagaStep> getStepsToCompensate() {
        // Return steps in reverse order for compensation
        List<SagaStep> steps = new ArrayList<>(completedSteps);
        steps.sort((a, b) -> b.ordinal() - a.ordinal());
        return steps;
    }

    // Getters and setters
    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public SagaStatus getStatus() { return status; }
    public void setStatus(SagaStatus status) { this.status = status; }

    public SagaStep getCurrentStep() { return currentStep; }
    public void setCurrentStep(SagaStep currentStep) { this.currentStep = currentStep; }

    public List<SagaStep> getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(List<SagaStep> completedSteps) {
        this.completedSteps = completedSteps;
    }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public double getPackageWeight() { return packageWeight; }
    public void setPackageWeight(double packageWeight) { this.packageWeight = packageWeight; }

    public LocalDateTime getRequestedDeliveryTime() { return requestedDeliveryTime; }
    public void setRequestedDeliveryTime(LocalDateTime requestedDeliveryTime) {
        this.requestedDeliveryTime = requestedDeliveryTime;
    }

    public int getMaxDeliveryTimeMinutes() { return maxDeliveryTimeMinutes; }
    public void setMaxDeliveryTimeMinutes(int maxDeliveryTimeMinutes) {
        this.maxDeliveryTimeMinutes = maxDeliveryTimeMinutes;
    }

    public String getDeliveryId() { return deliveryId; }
    public void setDeliveryId(String deliveryId) { this.deliveryId = deliveryId; }

    public String getDroneId() { return droneId; }
    public void setDroneId(String droneId) { this.droneId = droneId; }
}

/**
 * Status of the SAGA
 */
enum SagaStatus {
    STARTED,        // SAGA has started
    IN_PROGRESS,    // Executing steps
    COMPLETED,      // All steps completed successfully
    FAILED,         // A step failed
    COMPENSATING,   // Rolling back completed steps
    COMPENSATED     // Rollback completed
}

/**
 * Steps in the order processing SAGA
 */
enum SagaStep {
    ORDER_VALIDATION,      // Step 1: Validate order in customer service
    DELIVERY_SCHEDULING,   // Step 2: Schedule delivery in delivery service
    DRONE_ASSIGNMENT,      // Step 3: Assign drone in drone service
    COMPLETED              // All steps done
}