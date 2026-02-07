package com.sensoringestionservice.sensoringestionservice.ingestion.config;

/*
 * Project: Industrial IoT — Sensor Ingestion Service
 * File: KafkaConfig.java
 * Purpose: Provide a Spring-managed KafkaTemplate<String, String> so the
 *          controller (next step) can publish JSON messages to Kafka.
 *          Broker address comes from application.yml (spring.kafka.*).
 */

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration for publishing events to the Kafka backbone,
 * as required by the architecture (REST → Kafka; topic: sensor-readings).
 */
@Configuration
public class KafkaConfig {

    /**
     * Read bootstrap servers from Spring config (application.yml):
     * spring.kafka.bootstrap-servers: ${KAFKA_BROKERS:kafka:9092}
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Build the base producer properties map.
     * We use String keys (sensorId) and String values (JSON payloads).
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();

        // Where the producer connects (Kafka cluster entrypoint).
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Key = sensorId → ensure per-sensor ordering by partition key.
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Value will be JSON string for now (we can switch to a JSON serializer later).
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Reasonable local-dev defaults; align with application.yml (acks=all, retries, small linger/batch).
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16_384);

        return props;
    }

    /**
     * ProducerFactory creates KafkaProducer instances with our configuration.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory(Map<String, Object> producerConfigs) {
        return new DefaultKafkaProducerFactory<>(producerConfigs);
    }

    /**
     * KafkaTemplate is the high-level helper we will inject into our producer/service
     * and controller to send messages to Kafka.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}