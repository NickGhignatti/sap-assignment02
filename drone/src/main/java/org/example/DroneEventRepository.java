package org.example;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for storing and retrieving drone events from MongoDB.
 * Supports event sourcing pattern by maintaining a complete event history.
 */
@Repository
public interface DroneEventRepository extends MongoRepository<DroneEventDocument, String> {

    /**
     * Find all events for a specific drone, ordered by timestamp
     */
    List<DroneEventDocument> findByDroneIdOrderByTimestampAsc(String droneId);

    /**
     * Find all events for a specific order, ordered by timestamp
     */
    List<DroneEventDocument> findByOrderIdOrderByTimestampAsc(String orderId);

    /**
     * Find all events for a drone with version greater than specified
     */
    List<DroneEventDocument> findByDroneIdAndVersionGreaterThanOrderByVersionAsc(
            String droneId, long version);

    /**
     * Count events for a specific drone
     */
    long countByDroneId(String droneId);
}