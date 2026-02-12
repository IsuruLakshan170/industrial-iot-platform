import os

class Cfg:
    def __init__(self):
        self.KAFKA_BROKERS = os.getenv("KAFKA_BROKERS", "kafka:9092")
        self.TOPIC_IN = os.getenv("TOPIC_IN", "sensor-readings")
        self.TOPIC_OUT_METRICS = os.getenv("TOPIC_OUT_METRICS", "processed-metrics")
        self.TOPIC_OUT_ANOMALY = os.getenv("TOPIC_OUT_ANOMALY", "anomaly-detected")
        self.GROUP_ID = os.getenv("GROUP_ID", "stream-processor")

        self.WORKER_THREADS = int(os.getenv("WORKER_THREADS", "4"))
        self.QUEUE_MAXSIZE = int(os.getenv("QUEUE_MAXSIZE", "1000"))

        self.TEMP_MAX = float(os.getenv("TEMP_MAX", "80"))
        self.VIB_SPIKE_PCT = float(os.getenv("VIB_SPIKE_PCT", "40"))
        self.PRESS_MAX = float(os.getenv("PRESS_MAX", "120"))

        self.IDEMP_CACHE_MAX = int(os.getenv("IDEMP_CACHE_MAX", "100000"))
        self.IDEMP_TTL_SECONDS = int(os.getenv("IDEMP_TTL_SECONDS", "86400"))

        self.PORT = int(os.getenv("PORT", "8090"))

cfg = Cfg()