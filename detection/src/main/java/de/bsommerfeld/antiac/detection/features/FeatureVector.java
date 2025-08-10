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
    private final double minInterval;
    private final double maxInterval;

    // Momentum
    private final double cpsFirstHalf;
    private final double cpsSecondHalf;

    // Orientation deltas (degrees)
    private final double avgYawDelta;
    private final double stdYawDelta;
    private final double avgPitchDelta;
    private final double stdPitchDelta;
    private final double steadyAimRatio; // 0..1 fraction of small deltas

    // Pattern scores
    private final double jitter;      // variability normalized (0..1 typical)
    private final double burstiness;  // burst score (0..1 typical)
    private final int doubleClickCount; // intervals below configured threshold (see extractor)

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
        this(playerId, windowStartMillis, windowEndMillis, clickCount, cps,
                meanInterval, medianInterval, stdInterval, iqrInterval, jitter, burstiness,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

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
                         double burstiness,
                         double minInterval,
                         double maxInterval,
                         double cpsFirstHalf,
                         double cpsSecondHalf,
                         double avgYawDelta,
                         double stdYawDelta,
                         double avgPitchDelta,
                         double stdPitchDelta,
                         double steadyAimRatio,
                         int doubleClickCount) {
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
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.cpsFirstHalf = cpsFirstHalf;
        this.cpsSecondHalf = cpsSecondHalf;
        this.avgYawDelta = avgYawDelta;
        this.stdYawDelta = stdYawDelta;
        this.avgPitchDelta = avgPitchDelta;
        this.stdPitchDelta = stdPitchDelta;
        this.steadyAimRatio = steadyAimRatio;
        this.doubleClickCount = doubleClickCount;
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
    public double getMinInterval() { return minInterval; }
    public double getMaxInterval() { return maxInterval; }
    public double getCpsFirstHalf() { return cpsFirstHalf; }
    public double getCpsSecondHalf() { return cpsSecondHalf; }
    public double getAvgYawDelta() { return avgYawDelta; }
    public double getStdYawDelta() { return stdYawDelta; }
    public double getAvgPitchDelta() { return avgPitchDelta; }
    public double getStdPitchDelta() { return stdPitchDelta; }
    public double getSteadyAimRatio() { return steadyAimRatio; }
    public double getJitter() { return jitter; }
    public double getBurstiness() { return burstiness; }
    public int getDoubleClickCount() { return doubleClickCount; }

    public String toCsvHeader() {
        return "playerId,windowStartMillis,windowEndMillis,clickCount,cps,meanInterval,medianInterval,stdInterval,iqrInterval,jitter,burstiness,minInterval,maxInterval,cpsFirstHalf,cpsSecondHalf,avgYawDelta,stdYawDelta,avgPitchDelta,stdPitchDelta,steadyAimRatio,doubleClickCount";
    }

    public String toCsvRow() {
        return String.format(Locale.ROOT,
                "%s,%d,%d,%d,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%d",
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
                burstiness,
                minInterval,
                maxInterval,
                cpsFirstHalf,
                cpsSecondHalf,
                avgYawDelta,
                stdYawDelta,
                avgPitchDelta,
                stdPitchDelta,
                steadyAimRatio,
                doubleClickCount
        );
    }
}
