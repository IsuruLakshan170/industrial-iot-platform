import json
import threading
import queue
from datetime import datetime

from config import cfg
from consumer import StreamConsumer
from producer import Producers
from utils.logging import log
from utils.time import parse_ts
from processing.models import SensorReading
from processing.aggregator import RollingAggregator
from processing.rules import detect_anomalies, health_score
from processing.idempotency import SeenCache

# Work queue & components
work_q = queue.Queue(maxsize=cfg.QUEUE_MAXSIZE)
consumer = StreamConsumer(cfg.TOPIC_IN, cfg.GROUP_ID, cfg.KAFKA_BROKERS)
producers = Producers(cfg.KAFKA_BROKERS)
agg = RollingAggregator(window=10)
idemp = SeenCache(cfg.IDEMP_TTL_SECONDS, cfg.IDEMP_CACHE_MAX)

def to_reading(payload: dict) -> SensorReading:
    return SensorReading(
        event_id=payload["eventId"],
        sensor_id=payload["sensorId"],
        ts=parse_ts(payload["ts"]),
        temperature=float(payload["temperature"]),
        vibration=float(payload["vibration"]),
        pressure=float(payload["pressure"]),
        device_status=str(payload.get("device_status", "UNKNOWN"))
    )

def handler(msg) -> bool:
    try:
        payload = msg.value  # already JSON-decoded
        reading = to_reading(payload)

        # idempotency: drop duplicates
        if idemp.seen(reading.event_id):
            log("info", "duplicate-event", eventId=reading.event_id, sensorId=reading.sensor_id)
            return True  # commit and move on

        # compute rolling context
        prev_t, prev_v, prev_p = agg.last(reading.sensor_id)
        agg.add(reading.sensor_id, reading.temperature, reading.vibration, reading.pressure)
        avg_t, avg_v, avg_p = agg.averages(
            reading.sensor_id, reading.temperature, reading.vibration, reading.pressure
        )

        # health score & anomalies
        health = health_score(reading.temperature, reading.vibration, reading.pressure,
                              cfg.TEMP_MAX, cfg.PRESS_MAX)

        metrics = {
            "eventId": reading.event_id,
            "sensorId": reading.sensor_id,
            "ts": reading.ts.isoformat(),
            "temp": reading.temperature,
            "vib": reading.vibration,
            "press": reading.pressure,
            "avg_temp": avg_t,
            "avg_vib": avg_v,
            "avg_press": avg_p,
            "health": health
        }
        producers.send_metrics(cfg.TOPIC_OUT_METRICS, key=reading.sensor_id, value=metrics)

        anomalies = detect_anomalies(reading.temperature, prev_t, reading.vibration, prev_v,
                                     reading.pressure, cfg)
        for rule, severity, message in anomalies:
            anomaly = {
                "eventId": reading.event_id,
                "sensorId": reading.sensor_id,
                "ts": reading.ts.isoformat(),
                "rule": rule,
                "severity": severity,
                "message": message,
                "metric_snapshot": metrics
            }
            producers.send_anomaly(cfg.TOPIC_OUT_ANOMALY, key=reading.sensor_id, value=anomaly)

        producers.flush()
        log("info", "processed", eventId=reading.event_id, sensorId=reading.sensor_id, anomalies=len(anomalies))
        return True
    except Exception as e:
        log("error", "handler-failed", error=str(e))
        return False

def main():
    log("info", "stream-processor-start", brokers=cfg.KAFKA_BROKERS, inTopic=cfg.TOPIC_IN)
    consumer.loop(handler)

if __name__ == "__main__":
    main()