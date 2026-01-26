package org.example;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * MongoDB document to store drone events for event sourcing.
 * Each document represents a single event in a drone's lifecycle.
 */
@Document(collection = "drone_events")
public class DroneEventDocument {
    @Id
    private String id;

    @Indexed
    private String droneId;

    @Indexed
    private String orderId;

    private String eventType;
    private LocalDateTime timestamp;
    private long version;
    private String eventData; // JSON serialized event

    public DroneEventDocument() {}

    public DroneEventDocument(String droneId, String orderId, String eventType,
                              LocalDateTime timestamp, long version, String eventData) {
        this.droneId = droneId;
        this.orderId = orderId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.version = version;
        this.eventData = eventData;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDroneId() { return droneId; }
    public void setDroneId(String droneId) { this.droneId = droneId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }

    public String getEventData() { return eventData; }
    public void setEventData(String eventData) { this.eventData = eventData; }
}