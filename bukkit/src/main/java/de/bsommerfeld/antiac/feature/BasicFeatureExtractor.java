package de.bsommerfeld.antiac.feature;

import de.bsommerfeld.antiac.capture.ClickCollector;
import de.bsommerfeld.antiac.detection.features.FeatureExtractor;
import de.bsommerfeld.antiac.detection.features.FeatureVector;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Basic implementation computing common statistics from click timestamps.
 */
public class BasicFeatureExtractor implements FeatureExtractor {

    private final ClickCollector collector;

    @Inject
    public BasicFeatureExtractor(ClickCollector collector) {
        this.collector = collector;
    }

    @Override
    public FeatureVector extract(UUID playerId, List<Long> timestamps, long windowStartMillis, long windowEndMillis) {
        if (timestamps == null) timestamps = Collections.emptyList();
        int n = timestamps.size();
        double windowSec = Math.max(0.001, (windowEndMillis - windowStartMillis) / 1000.0);
        double cps = n / windowSec;

        if (n < 2) {
            return new FeatureVector(playerId, windowStartMillis, windowEndMillis, n, cps,
                    0, 0, 0, 0, 0, 0);
        }

        List<Long> intervals = new ArrayList<>(n - 1);
        for (int i = 1; i < n; i++) {
            long dt = Math.max(0, timestamps.get(i) - timestamps.get(i - 1));
            intervals.add(dt);
        }
        Collections.sort(intervals);

        double mean = mean(intervals);
        double median = median(intervals);
        double std = std(intervals, mean);
        double iqr = iqr(intervals);
        double jitter = mean > 0 ? clamp(std / mean, 0, 10) : 0; // coefficient of variation

        // Simple burstiness: fraction of intervals shorter than half the median
        double threshold = Math.max(1.0, median / 2.0);
        int bursts = 0;
        for (long x : intervals) if (x <= threshold) bursts++;
        double burstiness = bursts / (double) intervals.size();

        // Additional interval stats
        double minInterval = intervals.get(0);
        double maxInterval = intervals.get(intervals.size() - 1);
        // Double-click count: intervals under 110 ms considered double-clicks
        int doubleClickCount = 0;
        final long DOUBLE_CLICK_MS = 110L;
        for (long x : intervals) if (x <= DOUBLE_CLICK_MS) doubleClickCount++;

        // Momentum: split window into halves, compute CPS in each half
        long mid = (windowStartMillis + windowEndMillis) / 2L;
        int firstHalfClicks = 0;
        int secondHalfClicks = 0;
        for (long t : timestamps) {
            if (t < mid) firstHalfClicks++; else secondHalfClicks++;
        }
        double cpsFirstHalf = firstHalfClicks / Math.max(0.001, (mid - windowStartMillis) / 1000.0);
        double cpsSecondHalf = secondHalfClicks / Math.max(0.001, (windowEndMillis - mid) / 1000.0);

        // Crosshair movement deltas from collector orientations
        List<float[]> orientations = collector.getWindowOrientations(playerId, windowEndMillis);
        double avgYawDelta = 0, stdYawDelta = 0, avgPitchDelta = 0, stdPitchDelta = 0, steadyAimRatio = 0;
        if (orientations.size() >= 2) {
            int m = orientations.size();
            double[] yawD = new double[m - 1];
            double[] pitchD = new double[m - 1];
            double steadyThreshold = 0.7; // degrees
            int steadyCount = 0;
            for (int i = 1; i < m; i++) {
                float[] a = orientations.get(i - 1);
                float[] b = orientations.get(i);
                double dyaw = Math.abs(normAngleDeg(b[0] - a[0]));
                double dpitch = Math.abs(b[1] - a[1]);
                yawD[i - 1] = dyaw;
                pitchD[i - 1] = dpitch;
                double hypot = Math.hypot(dyaw, dpitch);
                if (hypot <= steadyThreshold) steadyCount++;
            }
            avgYawDelta = mean(yawD);
            stdYawDelta = std(yawD, avgYawDelta);
            avgPitchDelta = mean(pitchD);
            stdPitchDelta = std(pitchD, avgPitchDelta);
            steadyAimRatio = (m - 1) > 0 ? (steadyCount / (double) (m - 1)) : 0;
        }

        return new FeatureVector(playerId, windowStartMillis, windowEndMillis, n, cps,
                mean, median, std, iqr, jitter, burstiness,
                minInterval, maxInterval, cpsFirstHalf, cpsSecondHalf,
                avgYawDelta, stdYawDelta, avgPitchDelta, stdPitchDelta, steadyAimRatio,
                doubleClickCount);
    }

    private static double normAngleDeg(double d) {
        while (d > 180) d -= 360;
        while (d < -180) d += 360;
        return d;
    }

    private static double mean(List<Long> values) {
        if (values.isEmpty()) return 0;
        double sum = 0;
        for (long v : values) sum += v;
        return sum / values.size();
    }

    private static double mean(double[] values) {
        if (values.length == 0) return 0;
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    private static double median(List<Long> sorted) {
        if (sorted.isEmpty()) return 0;
        int m = sorted.size();
        if ((m & 1) == 1) return sorted.get(m / 2);
        return (sorted.get(m / 2 - 1) + sorted.get(m / 2)) / 2.0;
    }

    private static double std(List<Long> values, double mean) {
        if (values.size() < 2) return 0;
        double acc = 0;
        for (long v : values) {
            double d = v - mean;
            acc += d * d;
        }
        return Math.sqrt(acc / values.size());
    }

    private static double std(double[] values, double mean) {
        if (values.length < 2) return 0;
        double acc = 0;
        for (double v : values) {
            double d = v - mean;
            acc += d * d;
        }
        return Math.sqrt(acc / values.length);
    }

    private static double iqr(List<Long> sorted) {
        if (sorted.size() < 4) return 0;
        double q1 = percentile(sorted, 25);
        double q3 = percentile(sorted, 75);
        return Math.max(0, q3 - q1);
    }

    private static double percentile(List<Long> sorted, double pct) {
        if (sorted.isEmpty()) return 0;
        double rank = pct / 100.0 * (sorted.size() - 1);
        int low = (int) Math.floor(rank);
        int high = (int) Math.ceil(rank);
        if (low == high) return sorted.get(low);
        double weight = rank - low;
        return sorted.get(low) * (1 - weight) + sorted.get(high) * weight;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
