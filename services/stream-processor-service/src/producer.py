import json
from kafka import KafkaProducer

class Producers:
    def __init__(self, brokers: str):
        self._p = KafkaProducer(
            bootstrap_servers=brokers.split(","),
            key_serializer=lambda k: k.encode("utf-8"),
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
            linger_ms=5
        )

    def send_metrics(self, topic: str, key: str, value: dict):
        self._p.send(topic, key=key, value=value)

    def send_anomaly(self, topic: str, key: str, value: dict):
        self._p.send(topic, key=key, value=value)

    def flush(self):
        self._p.flush()