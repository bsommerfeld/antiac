package de.bsommerfeld.antiac.capture;

import de.bsommerfeld.antiac.logging.LogManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects raw click timestamps per player and maintains a rolling window.
 */
public class ClickCollector {
    private final Map<UUID, ArrayDeque<Long>> clicksByPlayer = new ConcurrentHashMap<>();
    private final long windowMillis;

    public ClickCollector(long windowMillis) {
        this.windowMillis = windowMillis;
    }

    public void recordClick(UUID playerId, long timestampMillis) {
        clicksByPlayer.computeIfAbsent(playerId, id -> new ArrayDeque<>()).addLast(timestampMillis);
        prune(playerId, timestampMillis);
    }

    public List<Long> getWindowTimestamps(UUID playerId, long nowMillis) {
        ArrayDeque<Long> q = clicksByPlayer.get(playerId);
        if (q == null || q.isEmpty()) return Collections.emptyList();
        prune(playerId, nowMillis);
        return new ArrayList<>(q);
    }

    private void prune(UUID playerId, long nowMillis) {
        ArrayDeque<Long> q = clicksByPlayer.get(playerId);
        if (q == null) return;
        long cutoff = nowMillis - windowMillis;
        while (!q.isEmpty() && q.peekFirst() < cutoff) {
            q.pollFirst();
        }
    }

    public long getWindowMillis() {
        return windowMillis;
    }
}
