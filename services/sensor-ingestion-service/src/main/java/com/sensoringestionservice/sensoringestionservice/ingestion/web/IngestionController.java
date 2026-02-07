package com.sensoringestionservice.sensoringestionservice.ingestion.web;

/*
 * Project: Industrial IoT — Sensor Ingestion Service
 * File: IngestionController.java
 * Purpose: REST controller that:
 *   - GET /api/v1/health                      → liveness check
 *   - POST /api/v1/sensors/{sensorId}/readings → validate JSON, publish to Kafka, return eventId
 *
 * This implements the architecture contract: REST → Kafka (topic: sensor-readings, key = sensorId).
 * Reference: Product documentation & roadmap.
 */

import com.sensoringestionservice.sensoringestionservice.ingestion.messaging.SensorEventProducer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * REST endpoints for the Sensor Ingestion Service.
 * Now wired to publish received readings to Kafka topic 'sensor-readings'
 * with key = sensorId, and returns a real eventId to the client.
 */
@RestController
@RequestMapping("/api/v1")
public class IngestionController {

    private final SensorEventProducer producer; // <-- NEW: dependency on the producer

    // Spring will inject the SensorEventProducer bean (it's annotated with @Component).
    public IngestionController(SensorEventProducer producer) {
        this.producer = producer;
    }

    /**
     * Quick liveness endpoint to verify app & HTTP routing are OK.
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    /**
     * Accept a sensor reading, validate it, publish to Kafka, and return eventId.
     *
     * Path: /api/v1/sensors/{sensorId}/readings
     * Behavior:
     *   - Validate request body fields
     *   - Publish an event to Kafka (topic: sensor-readings, key = sensorId)
     *   - Return 202 Accepted with { eventId, status: "queued" }
     *
     * This fulfills the ingestion responsibility defined in the architecture docs.
     */
    @PostMapping("/sensors/{sensorId}/readings")
    public ResponseEntity<?> ingestReading(
            @PathVariable("sensorId") String sensorId,
            @RequestBody @Valid SensorReadingRequest request
    ) {
        try {
            // 1) Publish to Kafka and get the eventId back
            String eventId = producer.publishReading(
                    sensorId,
                    request.getTimestamp(),
                    request.getTemperature(),
                    request.getVibration(),
                    request.getPressure(),
                    request.getDevice_status()
            );

            // 2) Build response: include eventId so callers can correlate downstream processing
            Map<String, Object> body = new HashMap<>();
            body.put("eventId", eventId);
            body.put("status", "queued");

            // 3) 202 Accepted because processing is async (event-driven via Kafka)
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);

        } catch (Exception e) {
            // If publishing fails for any reason, return 500 (we can refine this later).
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to enqueue reading");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Minimal request DTO with validation.
     * Fields match the 'sensor-readings' topic contract in the product document.
     */
    public static class SensorReadingRequest {
        @NotNull(message = "timestamp is required")
        private Instant timestamp;

        @NotNull(message = "temperature is required")
        private Double temperature;

        @NotNull(message = "vibration is required")
        private Double vibration;

        @NotNull(message = "pressure is required")
        private Double pressure;

        @NotBlank(message = "device_status is required")
        private String device_status;

        // Getters & setters
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }

        public Double getVibration() { return vibration; }
        public void setVibration(Double vibration) { this.vibration = vibration; }

        public Double getPressure() { return pressure; }
        public void setPressure(Double pressure) { this.pressure = pressure; }

        public String getDevice_status() { return device_status; }
        public void setDevice_status(String device_status) { this.device_status = device_status; }
    }
}