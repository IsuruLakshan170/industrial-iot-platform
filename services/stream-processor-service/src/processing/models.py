from dataclasses import dataclass
from datetime import datetime

@dataclass(frozen=True)
class SensorReading:
    event_id: str
    sensor_id: str
    ts: datetime
    temperature: float
    vibration: float
    pressure: float
    device_status: str

@dataclass
class ProcessedMetrics:
    event_id: str
    sensor_id: str
    ts: datetime
    temp: float
    vib: float
    press: float
    avg_temp: float
    avg_vib: float
    avg_press: float
    health: int  # 0..100