package com.sensoringestionservice.sensoringestionservice.ingestion.web;



import com.sensoringestionservice.sensoringestionservice.ingestion.domain.SensorReadingEvent;
import com.sensoringestionservice.sensoringestionservice.ingestion.messaging.SensorEventPublisher;
import com.sensoringestionservice.sensoringestionservice.ingestion.web.dto.EventEnqueuedResponse;
import com.sensoringestionservice.sensoringestionservice.ingestion.web.dto.SensorReadingRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class IngestionController {

    private final SensorEventPublisher publisher;

    public IngestionController(SensorEventPublisher publisher) {
        this.publisher = publisher;
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @PostMapping("/sensors/{sensorId}/readings")
    public ResponseEntity<EventEnqueuedResponse> ingestReading(
            @PathVariable("sensorId")
            @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "sensorId has invalid characters")
            String sensorId,
            @RequestBody @Valid SensorReadingRequest req
    ) {
        // Create a correlation/event ID
        String eventId = UUID.randomUUID().toString();

        // Build the typed event
        SensorReadingEvent event = new SensorReadingEvent(
                eventId,
                sensorId,
                // tolerate null timestamp by defaulting to 'now' (optional design choice)
                (req.getTimestamp() != null ? req.getTimestamp() : Instant.now()),
                req.getTemperature(),
                req.getVibration(),
                req.getPressure(),
                req.getDeviceStatus()
        );

        // Publish to Kafka
        publisher.publish(event);

        // 202 for async processing
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new EventEnqueuedResponse(eventId, "queued"));
    }
}