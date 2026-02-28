package org.example;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderMessageContractTest {

    @Test
    void testOrderMessageSerializationContract() throws Exception {
        // Arrange: Create the object in the "Customer" context
        LocalDateTime time = LocalDateTime.of(2026, 5, 10, 14, 30);
        OrderMessage originalMessage = new OrderMessage(
                "order-abc", "customer-XYZ", "Rome", "Milan",
                15.5, time, 120
        );

        // Act: Serialize to JSON bytes (Simulating RabbitMQ transmission)
        byte[] jsonBytes = MessageSerializer.serialize(originalMessage);

        // Deserialize back to Object (Simulating Delivery/Drone reception)
        OrderMessage deserializedMessage = MessageSerializer.deserialize(jsonBytes, OrderMessage.class);

        // Assert: Ensure no data was lost or corrupted during the transmission
        assertEquals(originalMessage.orderId(), deserializedMessage.orderId());
        assertEquals(originalMessage.packageWeight(), deserializedMessage.packageWeight());
        assertEquals(originalMessage.requestedDeliveryTime(), deserializedMessage.requestedDeliveryTime());
    }
}