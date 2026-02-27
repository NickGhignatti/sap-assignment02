package org.example;

/**
 * Steps in the order processing SAGA
 */
public enum SagaStep {
    ORDER_VALIDATION,      // Step 1: Validate order in customer service
    DELIVERY_SCHEDULING,   // Step 2: Schedule delivery in delivery service
    DRONE_ASSIGNMENT,      // Step 3: Assign drone in drone service
    COMPLETED              // All steps done
}
