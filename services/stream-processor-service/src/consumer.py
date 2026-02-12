# services/stream-processor-service/src/consumer.py
import json
from kafka import KafkaConsumer
from typing import Callable

class StreamConsumer:
    def __init__(self, in_topic: str, group_id: str, brokers: str):
        """
        Create a KafkaConsumer subscribed to one topic.
        - enable_auto_commit=False so *we* control when to commit.
        - auto_offset_reset="earliest" so a fresh group reads from the beginning.
        - consumer_timeout_ms gives the iterator a chance to break periodically.
        """
        self.consumer = KafkaConsumer(
            in_topic,
            group_id=group_id,
            bootstrap_servers=brokers.split(","),
            enable_auto_commit=False,
            auto_offset_reset="earliest",
            value_deserializer=lambda m: json.loads(m.decode("utf-8")),
            key_deserializer=lambda m: m.decode("utf-8") if m else None,
            consumer_timeout_ms=1000
        )

    def _iter_messages(self):
        """
        Private generator that yields messages from Kafka.
        The consumer blocks until messages arrive, but because
        consumer_timeout_ms is set, the for-loop exits periodically,
        letting the outer loop continue (keeps things responsive).
        """
        while True:
            for msg in self.consumer:
                yield msg
            # loop again after timeout to stay responsive

    def loop(self, handler: Callable):
        """
        Main loop.
        - Calls handler(msg) for each message.
        - If handler returns True => processing succeeded => commit.
        - If handler returns False => do NOT commit (message may be retried).
        """
        for msg in self._iter_messages():
            ok = False
            try:
                ok = handler(msg)
            except Exception as e:
                # Let the caller handle logging; we keep this minimal here.
                ok = False

            if ok:
                # Commit the latest processed offsets for this consumer group.
                # In kafka-python, calling commit() with no args commits the
                # last returned offsets on all assigned partitions.
                self.consumer.commit()