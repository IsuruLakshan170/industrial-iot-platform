
# 🏭 Industrial IoT Equipment Health Monitoring System

## 📘 Executive Summary
This system provides real-time health monitoring for industrial equipment using IoT sensor ingestion, concurrent stream processing, anomaly detection, and alerting. Built with Java, Python, Kafka, PostgreSQL, and Docker, it is designed for scalability, fault tolerance, and observability.

## 🎯 Goals
- Real-time ingestion of telemetry (temperature, vibration, pressure).
- Concurrent stream processing and anomaly detection.
- Event-driven microservices using Kafka.
- Durable metrics and audit storage in PostgreSQL.
- Alert pipeline with retries.

## 🚫 Non-Goals (v1)
- No frontend UI.
- No complex ML models.
- No device provisioning.
- No advanced multi-tenant RBAC.

## 👥 Target Users
- Operations Engineers
- Reliability Engineers
- SRE/DevOps
- Developers

## 🔧 Key Use Cases
- Ingest simulated IoT sensor readings via REST.
- Process readings concurrently and compute rolling metrics.
- Detect anomalies using rule thresholds and spike detection.

## 🧱 Architecture Overview

<img width="1312" height="1202" alt="image" src="https://github.com/user-attachments/assets/18835924-d4b8-4763-a98b-d8357df1c21d" />

Event-driven microservices architecture:
- **Sensor Ingestion Service (Java)** → Publishes to Kafka topic `sensor-readings`.
- **Stream Processor (Python)** → Consumes events, emits `processed-metrics` & `anomaly-detected`.
- **Metrics Service (Java)** → Persists metrics to PostgreSQL.
- **Alert Service (Python)** → Creates alerts, retries, audit logs.

## 🏁 Completed Work (v0.1 → v1 Progress)
### ✔ Infrastructure

<img width="1290" height="799" alt="Docker Compose Infrastructure" src="https://github.com/user-attachments/assets/41afb510-e264-483d-b957-20538a44a042" />

- Kafka, Zookeeper, PostgreSQL running via Docker Compose.
- Can create/list topics and connect to PostgreSQL.

### ✔ Sensor Ingestion Service

<img width="1289" height="502" alt="Sensor Ingestion Service – Working Flow" src="https://github.com/user-attachments/assets/8eee21ab-cf21-40ca-a757-9aa47019012d" />

- Accepts REST input, validates, publishes to Kafka.
- Returns 202 with eventId.

### ✔ Stream Processor

<img width="1397" height="1014" alt="Stream Processor Service" src="https://github.com/user-attachments/assets/8a83019a-875b-4753-aca5-1bd0b49ba260" />

- Concurrent processing with rules-based anomaly detection.

### ✔ Metrics Service
- PostgreSQL schema implemented.
- Query endpoints provided.

### ✔ Alert Service
- Retry with backoff + audit logs.

## 🔄 Roadmap
### 📌 v1.1
- Dead-letter queues
- Retry topics
- OpenTelemetry tracing

### 📌 v1.2
- Redis cache
- Windowed aggregations
- RBAC improvements

### 📌 v2
- ML anomaly detection
- Multi-tenant ingestion
- Grafana dashboards

## ▶ How to Run
```
docker compose up -d --build
```
1. Register sensor (optional)
2. Send readings via REST
3. Tail processor + alert logs
4. Query metrics

## 🎉 Conclusion
This project implements a production-inspired Industrial IoT pipeline capable of ingestion → Kafka streaming → anomaly detection → metrics persistence → alerting.
