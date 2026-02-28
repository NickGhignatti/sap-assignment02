package org.example;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderSagaOrchestratorTest {

    private OrderSagaRepository sagaRepository;
    private RabbitTemplate rabbitTemplate;
    private OrderSagaOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        // Mock dependencies
        sagaRepository = mock(OrderSagaRepository.class);
        rabbitTemplate = mock(RabbitTemplate.class);
        // Use a SimpleMeterRegistry so metrics don't crash
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        orchestrator = new OrderSagaOrchestrator(sagaRepository, rabbitTemplate, meterRegistry);
    }

    @Test
    void testHandleSagaEvents_DeliveryScheduled() {
        // Arrange
        String orderId = "order-123";
        String deliveryId = "del-456";

        OrderSagaState mockSaga = new OrderSagaState(
                "saga-1", orderId, "cust-1", "A", "B", 10.0, LocalDateTime.now(), 60
        );

        mockSaga.setCurrentStep(SagaStep.DELIVERY_SCHEDULING);

        when(sagaRepository.findByOrderId(orderId)).thenReturn(Optional.of(mockSaga));

        DeliveryScheduledEvent event = new DeliveryScheduledEvent(
                "saga-1", orderId, deliveryId, LocalDateTime.now()
        );

        // Act
        orchestrator.handleSagaEvents(event);

        // Assert
        assertEquals(deliveryId, mockSaga.getDeliveryId());
        assertEquals(SagaStep.DRONE_ASSIGNMENT, mockSaga.getCurrentStep());

        // Verify repository saved the updated state
        verify(sagaRepository, times(1)).save(mockSaga);
    }
}