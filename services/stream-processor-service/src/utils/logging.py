import json
import sys
from datetime import datetime

def log(level: str, msg: str, **kwargs):
    event = {
        "ts": datetime.utcnow().isoformat() + "Z",
        "level": level.upper(),
        "msg": msg,
        **kwargs
    }
    sys.stdout.write(json.dumps(event) + "\n")
    sys.stdout.flush()