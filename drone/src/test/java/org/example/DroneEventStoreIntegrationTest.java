package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest(properties = "de.flapdoodle.mongodb.embedded.version=6.0.10")
@Import(DroneEventStore.class)
class DroneEventStoreIntegrationTest {

    @Autowired
    private DroneEventStore droneEventStore;

    @Test
    void testSaveAndRebuildDroneFromEvents() {
        // Arrange
        String droneId = "drone-999";
        String orderId = "order-777";

        DroneCreatedEvent createdEvent = new DroneCreatedEvent(
                droneId, orderId, "Rome", "Milan", 2.5,
                LocalDateTime.now(), 30, LocalDateTime.now(), 0
        );

        DroneDispatchedEvent dispatchedEvent = new DroneDispatchedEvent(
                droneId, orderId, LocalDateTime.now(), LocalDateTime.now(), 1
        );

        // Act - Salva nel DB Fongo
        droneEventStore.saveEvent(createdEvent);
        droneEventStore.saveEvent(dispatchedEvent);

        // Rebuild dal DB
        Drone rebuiltDrone = droneEventStore.rebuildDroneFromEvents(droneId);

        // Assert
        assertNotNull(rebuiltDrone);
        assertEquals(droneId, rebuiltDrone.getId());
        assertEquals(DroneState.InTransit, rebuiltDrone.getState());
    }
}