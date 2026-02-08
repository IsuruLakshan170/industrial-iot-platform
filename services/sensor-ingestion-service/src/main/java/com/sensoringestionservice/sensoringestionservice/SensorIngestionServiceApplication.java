package com.sensoringestionservice.sensoringestionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan // pick up @ConfigurationProperties classes
public class SensorIngestionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SensorIngestionServiceApplication.class, args);
    }
}