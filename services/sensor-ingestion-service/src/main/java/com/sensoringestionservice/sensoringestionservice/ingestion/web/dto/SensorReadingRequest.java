package com.sensoringestionservice.sensoringestionservice.ingestion.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public class SensorReadingRequest {

    @NotNull(message = "timestamp is required")
    private Instant timestamp;

    @NotNull(message = "temperature is required")
    private Double temperature;

    @NotNull(message = "vibration is required")
    private Double vibration;

    @NotNull(message = "pressure is required")
    private Double pressure;

    @NotBlank(message = "device_status is required")
    @JsonProperty("device_status")
    private String deviceStatus;

    // Optional basic sanity checks (non-negative)
    @PositiveOrZero(message = "temperature must be >= 0")
    public Double getTemperature() { return temperature; }

    @PositiveOrZero(message = "vibration must be >= 0")
    public Double getVibration() { return vibration; }

    @PositiveOrZero(message = "pressure must be >= 0")
    public Double getPressure() { return pressure; }

    // getters & setters
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public void setVibration(Double vibration) { this.vibration = vibration; }
    public void setPressure(Double pressure) { this.pressure = pressure; }
    public String getDeviceStatus() { return deviceStatus; }
    public void setDeviceStatus(String deviceStatus) { this.deviceStatus = deviceStatus; }
}