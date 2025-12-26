package org.example;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DroneEventRepository extends MongoRepository<DroneEventDocument, String> {
    List<DroneEventDocument> findByDroneIdOrderByTimestampAsc(String droneId);
}