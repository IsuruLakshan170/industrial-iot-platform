package com.sensoringestionservice.sensoringestionservice.ingestion.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record SensorReadingEvent(
        String eventId,
        String sensorId,
        Instant ts,
        Double temperature,
        Double vibration,
        Double pressure,
        @JsonProperty("device_status") String deviceStatus
) {}