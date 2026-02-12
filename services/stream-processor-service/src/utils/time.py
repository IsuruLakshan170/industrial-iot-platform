from datetime import datetime, timezone

def parse_ts(s: str) -> datetime:
    # ISO-8601 like: "2026-02-05T16:07:00Z"
    try:
        if s.endswith("Z"):
            return datetime.fromisoformat(s.replace("Z", "+00:00"))
        return datetime.fromisoformat(s).astimezone(timezone.utc)
    except Exception:
        return datetime.now(timezone.utc)