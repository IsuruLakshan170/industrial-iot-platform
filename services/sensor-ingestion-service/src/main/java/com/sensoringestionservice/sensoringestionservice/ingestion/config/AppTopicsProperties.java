package com.sensoringestionservice.sensoringestionservice.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app.topics")
public record AppTopicsProperties(
        @NotBlank String sensorReadings
) {}