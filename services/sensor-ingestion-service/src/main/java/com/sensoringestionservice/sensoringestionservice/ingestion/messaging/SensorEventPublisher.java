package com.sensoringestionservice.sensoringestionservice.ingestion.messaging;


import com.sensoringestionservice.sensoringestionservice.ingestion.domain.SensorReadingEvent;

public interface SensorEventPublisher {
    String publish(SensorReadingEvent event);
}