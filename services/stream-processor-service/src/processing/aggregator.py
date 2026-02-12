from collections import defaultdict, deque
import statistics as st

class RollingAggregator:
    """
    Keeps a small rolling window of recent values per sensor
    to compute simple averages. For dev we use a deque of N items.
    """
    def __init__(self, window:int=10):
        self._t = defaultdict(lambda: deque(maxlen=window))
        self._v = defaultdict(lambda: deque(maxlen=window))
        self._p = defaultdict(lambda: deque(maxlen=window))

    def add(self, sid:str, t:float, v:float, p:float):
        self._t[sid].append(t)
        self._v[sid].append(v)
        self._p[sid].append(p)

    def last(self, sid:str):
        prev_t = self._t[sid][-1] if self._t[sid] else None
        prev_v = self._v[sid][-1] if self._v[sid] else None
        prev_p = self._p[sid][-1] if self._p[sid] else None
        return prev_t, prev_v, prev_p

    def averages(self, sid:str, fallback_t:float, fallback_v:float, fallback_p:float):
        avg_t = st.fmean(self._t[sid]) if self._t[sid] else fallback_t
        avg_v = st.fmean(self._v[sid]) if self._v[sid] else fallback_v
        avg_p = st.fmean(self._p[sid]) if self._p[sid] else fallback_p
        return avg_t, avg_v, avg_p