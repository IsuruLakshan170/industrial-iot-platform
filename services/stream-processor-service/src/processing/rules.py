def health_score(temp, vib, press, t_max, p_max):
    # Start with 100; subtract penalties if over thresholds
    score = 100
    if temp > t_max:
        score -= 30
    if press > p_max:
        score -= 30
    # vibration spike handled separately
    return max(0, score)

def detect_anomalies(temp, prev_temp, vib, prev_vib, press, cfg):
    anomalies = []
    if temp > cfg.TEMP_MAX:
        anomalies.append(("TEMP_THRESHOLD", "HIGH", f"Temp {temp} > {cfg.TEMP_MAX}"))
    if press > cfg.PRESS_MAX:
        anomalies.append(("PRESS_THRESHOLD", "HIGH", f"Pressure {press} > {cfg.PRESS_MAX}"))
    if prev_vib is not None and prev_vib > 0:
        pct = ((vib - prev_vib) / prev_vib) * 100
        if pct > cfg.VIB_SPIKE_PCT:
            anomalies.append(("VIB_SPIKE", "MEDIUM", f"Vibration +{pct:.1f}% > {cfg.VIB_SPIKE_PCT}%"))
    return anomalies