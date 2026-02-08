package com.sensoringestionservice.sensoringestionservice.ingestion.messaging;


import com.sensoringestionservice.sensoringestionservice.ingestion.config.AppTopicsProperties;
import com.sensoringestionservice.sensoringestionservice.ingestion.domain.SensorReadingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SensorEventProducer implements SensorEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SensorEventProducer.class);

    private final KafkaTemplate<String, SensorReadingEvent> kafkaTemplate;
    private final AppTopicsProperties topics;

    public SensorEventProducer(KafkaTemplate<String, SensorReadingEvent> kafkaTemplate,
                               AppTopicsProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    @Override
    public String publish(SensorReadingEvent event) {
        // Key by sensorId to preserve per-sensor ordering
        var future = kafkaTemplate.send(topics.sensorReadings(), event.sensorId(), event);

        // async completion logging
        future.whenComplete((result, ex) -> {
            if (ex == null && result != null) {
                log.info("Published eventId={} topic={} partition={} offset={}",
                        event.eventId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish eventId={} topic={} - {}",
                        event.eventId(), topics.sensorReadings(),
                        (ex != null ? ex.getMessage() : "unknown error"), ex);
            }
        });

        // We immediately return the eventId for correlation
        return event.eventId();
    }
}
