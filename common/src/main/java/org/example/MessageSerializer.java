package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for serializing/deserializing messages to/from JSON.
 * Use this in all microservices for consistent serialization.
 */
public class MessageSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Register module to handle Java 8 date/time types (LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        // Write dates as ISO-8601 strings instead of timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Serialize a message object to JSON byte array (for RabbitMQ)
     */
    public static <T> byte[] serialize(T message) throws Exception {
        return mapper.writeValueAsBytes(message);
    }

    /**
     * Serialize a message object to JSON string (for debugging)
     */
    public static <T> String serializeToString(T message) throws Exception {
        return mapper.writeValueAsString(message);
    }

    /**
     * Deserialize JSON byte array to message object (from RabbitMQ)
     */
    public static <T> T deserialize(byte[] data, Class<T> messageClass) throws Exception {
        return mapper.readValue(data, messageClass);
    }

    /**
     * Deserialize JSON string to message object (for debugging)
     */
    public static <T> T deserializeFromString(String json, Class<T> messageClass) throws Exception {
        return mapper.readValue(json, messageClass);
    }
}
