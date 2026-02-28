package org.example;

/**
 * Status of the SAGA
 */
public enum SagaStatus {
    STARTED,        // SAGA has started
    IN_PROGRESS,    // Executing steps
    COMPLETED,      // All steps completed successfully
    FAILED,         // A step failed
    COMPENSATING,   // Rolling back completed steps
    COMPENSATED     // Rollback completed
}
