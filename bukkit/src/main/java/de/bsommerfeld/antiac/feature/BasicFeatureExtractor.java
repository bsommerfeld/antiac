package de.bsommerfeld.antiac.feature;

import de.bsommerfeld.antiac.detection.features.FeatureExtractor;
import de.bsommerfeld.antiac.detection.features.FeatureVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Basic implementation computing common statistics from click timestamps.
 */
public class BasicFeatureExtractor implements FeatureExtractor {

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

        return new FeatureVector(playerId, windowStartMillis, windowEndMillis, n, cps,
                mean, median, std, iqr, jitter, burstiness);
    }

    private static double mean(List<Long> values) {
        if (values.isEmpty()) return 0;
        double sum = 0;
        for (long v : values) sum += v;
        return sum / values.size();
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
