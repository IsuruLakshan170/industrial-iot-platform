import time

class SeenCache:
    """
    Very simple in-memory dedupe cache; good enough for dev.
    """
    def __init__(self, ttl_seconds:int, max_size:int):
        self.ttl = ttl_seconds
        self.max = max_size
        self.store = {}  # event_id -> expire_at

    def seen(self, event_id:str)->bool:
        now = time.time()
        # expire a small batch if oversized
        if len(self.store) > self.max:
            for k,v in list(self.store.items())[: self.max//10]:
                if v < now:
                    self.store.pop(k, None)
        # already seen and not expired?
        if event_id in self.store and self.store[event_id] > now:
            return True
        # mark as seen
        self.store[event_id] = now + self.ttl
        return False