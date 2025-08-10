package de.bsommerfeld.antiac.detection.features;

import java.util.List;
import java.util.UUID;

/**
 * Extracts a FeatureVector from a sequence of click timestamps (millis) within a window.
 */
public interface FeatureExtractor {
    FeatureVector extract(UUID playerId, List<Long> timestamps, long windowStartMillis, long windowEndMillis);
}
