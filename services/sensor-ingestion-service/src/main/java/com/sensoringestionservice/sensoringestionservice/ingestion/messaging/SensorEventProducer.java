package com.sensoringestionservice.sensoringestionservice.ingestion.messaging;

/*
 * Project: Industrial IoT — Sensor Ingestion Service
 * File: SensorEventProducer.java
 * Purpose: Build an event (JSON) and publish it to Kafka topic 'sensor-readings'
 *          with key = sensorId, as defined in the product architecture:
 *          REST → Kafka; partition by sensorId; return eventId to the client.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;       // Reads topic name from application.yml.
import org.springframework.kafka.core.KafkaTemplate;            // High-level Kafka producer.
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SensorEventProducer {

    private static final Logger log = LoggerFactory.getLogger(SensorEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;  // We configured this in KafkaConfig.java
    private final ObjectMapper objectMapper;                    // Spring Boot auto-configures Jackson ObjectMapper.

    // Read topic name from application.yml → app.topics.sensor-readings
    @Value("${app.topics.sensor-readings}")
    private String sensorReadingsTopic;

    public SensorEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish a sensor reading to Kafka and return the generated eventId.
     *
     * @param sensorId       Device identifier (used as Kafka key for partitioning & per-sensor ordering)
     * @param timestamp      Reading timestamp (Instant)
     * @param temperature    Reading (Double)
     * @param vibration      Reading (Double)
     * @param pressure       Reading (Double)
     * @param deviceStatus   Status field from request payload
     * @return eventId (UUID) so the caller can correlate logs/processing later
     */
    public String publishReading(String sensorId,
                                 Instant timestamp,
                                 Double temperature,
                                 Double vibration,
                                 Double pressure,
                                 String deviceStatus) {
        try {
            // 1) Generate correlation ID for this event
            String eventId = UUID.randomUUID().toString();

            // 2) Build the payload following the data contract for 'sensor-readings'
            //    {eventId, sensorId, ts, temperature, vibration, pressure, device_status}
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", eventId);
            payload.put("sensorId", sensorId);
            payload.put("ts", timestamp);            // ISO-8601 by default via Jackson
            payload.put("temperature", temperature);
            payload.put("vibration", vibration);
            payload.put("pressure", pressure);
            payload.put("device_status", deviceStatus);

            // 3) Serialize to JSON
            String json = objectMapper.writeValueAsString(payload);

            // 4) Publish to Kafka (topic = sensor-readings, key = sensorId)
            //    Using key=sensorId preserves per-sensor event order, as per topic design.
            kafkaTemplate.send(sensorReadingsTopic, sensorId, json)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Published eventId={} to topic={}, partition={}, offset={}",
                                    eventId,
                                    sensorReadingsTopic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to publish eventId={} to topic={}: {}",
                                    eventId, sensorReadingsTopic, ex.getMessage(), ex);
                        }
                    });

            // 5) Return eventId to caller immediately (async publish)
            return eventId;

        } catch (Exception e) {
            // For now, surface a runtime error; controller will map to 500.
            // Later we can add better error mapping/retries if Kafka is temporarily unavailable.
            log.error("Error building/publishing sensor event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish sensor event", e);
        }
    }
}