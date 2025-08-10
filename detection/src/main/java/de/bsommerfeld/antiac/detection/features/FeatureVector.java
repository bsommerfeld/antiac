package de.bsommerfeld.antiac.detection.features;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable feature vector describing a player's recent clicking behavior.
 * Designed to be used by detection checks and external ML pipelines.
 */
public final class FeatureVector {
    private final UUID playerId;
    private final long windowStartMillis;
    private final long windowEndMillis;

    // Basic counts
    private final int clickCount;
    private final double cps;

    // Inter-click interval stats (millis)
    private final double meanInterval;
    private final double medianInterval;
    private final double stdInterval;
    private final double iqrInterval;

    // Pattern scores
    private final double jitter;      // variability normalized (0..1 typical)
    private final double burstiness;  // burst score (0..1 typical)

    public FeatureVector(UUID playerId,
                         long windowStartMillis,
                         long windowEndMillis,
                         int clickCount,
                         double cps,
                         double meanInterval,
                         double medianInterval,
                         double stdInterval,
                         double iqrInterval,
                         double jitter,
                         double burstiness) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.windowStartMillis = windowStartMillis;
        this.windowEndMillis = windowEndMillis;
        this.clickCount = clickCount;
        this.cps = cps;
        this.meanInterval = meanInterval;
        this.medianInterval = medianInterval;
        this.stdInterval = stdInterval;
        this.iqrInterval = iqrInterval;
        this.jitter = jitter;
        this.burstiness = burstiness;
    }

    public UUID getPlayerId() { return playerId; }
    public long getWindowStartMillis() { return windowStartMillis; }
    public long getWindowEndMillis() { return windowEndMillis; }
    public int getClickCount() { return clickCount; }
    public double getCps() { return cps; }
    public double getMeanInterval() { return meanInterval; }
    public double getMedianInterval() { return medianInterval; }
    public double getStdInterval() { return stdInterval; }
    public double getIqrInterval() { return iqrInterval; }
    public double getJitter() { return jitter; }
    public double getBurstiness() { return burstiness; }

    public String toCsvHeader() {
        return "playerId,windowStartMillis,windowEndMillis,clickCount,cps,meanInterval,medianInterval,stdInterval,iqrInterval,jitter,burstiness";
    }

    public String toCsvRow() {
        return String.format(Locale.ROOT,
                "%s,%d,%d,%d,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f",
                playerId,
                windowStartMillis,
                windowEndMillis,
                clickCount,
                cps,
                meanInterval,
                medianInterval,
                stdInterval,
                iqrInterval,
                jitter,
                burstiness
        );
    }
}
