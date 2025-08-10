package de.bsommerfeld.antiac.capture;

import de.bsommerfeld.antiac.logging.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Collects raw click timestamps per player and maintains a rolling window.
 * Also tracks basic orientation (yaw/pitch) captured at click time for crosshair movement analysis.
 */
public class ClickCollector {
    private final Map<UUID, Deque<Long>> clicksByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<float[]>> orientationByPlayer = new ConcurrentHashMap<>();
    private final long windowMillis;

    public ClickCollector(long windowMillis) {
        this.windowMillis = windowMillis;
    }

    /**
     * Backwards-compatible API recording a click without orientation data.
     */
    public void recordClick(UUID playerId, long timestampMillis) {
        recordClick(playerId, timestampMillis, Float.NaN, Float.NaN);
    }

    /**
     * Records a click with optional orientation (yaw/pitch in degrees). Use NaN when not available.
     */
    public void recordClick(UUID playerId, long timestampMillis, float yaw, float pitch) {
        clicksByPlayer.computeIfAbsent(playerId, id -> new ConcurrentLinkedDeque<>()).addLast(timestampMillis);
        orientationByPlayer.computeIfAbsent(playerId, id -> new ConcurrentLinkedDeque<>()).addLast(new float[]{yaw, pitch});
        prune(playerId, timestampMillis);
    }

    public List<Long> getWindowTimestamps(UUID playerId, long nowMillis) {
        Deque<Long> q = clicksByPlayer.get(playerId);
        if (q == null || q.isEmpty()) return Collections.emptyList();
        prune(playerId, nowMillis);
        return new ArrayList<>(q);
    }

    /**
     * Returns orientation pairs aligned with timestamps in the current window.
     */
    public List<float[]> getWindowOrientations(UUID playerId, long nowMillis) {
        Deque<float[]> oq = orientationByPlayer.get(playerId);
        Deque<Long> tq = clicksByPlayer.get(playerId);
        if (oq == null || tq == null || oq.isEmpty() || tq.isEmpty()) return Collections.emptyList();
        prune(playerId, nowMillis);
        // After prune, sizes should be aligned; create a copy
        return new ArrayList<>(oq);
    }

    private void prune(UUID playerId, long nowMillis) {
        Deque<Long> tq = clicksByPlayer.get(playerId);
        Deque<float[]> oq = orientationByPlayer.get(playerId);
        if (tq == null) return;
        long cutoff = nowMillis - windowMillis;
        while (!tq.isEmpty() && tq.peekFirst() < cutoff) {
            tq.pollFirst();
            if (oq != null && !oq.isEmpty()) {
                oq.pollFirst();
            }
        }
        // Ensure orientation queue does not exceed timestamps size
        if (oq != null) {
            while (oq.size() > tq.size()) {
                oq.pollFirst();
            }
        }
    }

    public long getWindowMillis() {
        return windowMillis;
    }
}
