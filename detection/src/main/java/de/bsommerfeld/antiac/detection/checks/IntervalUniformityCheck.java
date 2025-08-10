package de.bsommerfeld.antiac.detection.checks;

import de.bsommerfeld.antiac.detection.Check;
import de.bsommerfeld.antiac.detection.CheckResult;
import de.bsommerfeld.antiac.detection.SimpleCheckResult;
import de.bsommerfeld.antiac.detection.features.FeatureVector;

/**
 * Detects unnaturally uniform clicking patterns (very low jitter/iqr) at notable CPS.
 */
public final class IntervalUniformityCheck implements Check<FeatureVector, Double> {
    private final double minCpsToConsider;
    private final double maxJitterCv; // coefficient of variation threshold
    private final double maxIqrMillis; // optional IQR threshold in ms

    public IntervalUniformityCheck(double minCpsToConsider, double maxJitterCv, double maxIqrMillis) {
        this.minCpsToConsider = minCpsToConsider;
        this.maxJitterCv = maxJitterCv;
        this.maxIqrMillis = maxIqrMillis;
    }

    @Override
    public CheckResult<Double> execute(FeatureVector input) {
        double cps = input.getCps();
        double jitter = input.getJitter();
        double iqr = input.getIqrInterval();
        boolean flagged = cps >= minCpsToConsider && (jitter <= maxJitterCv || iqr <= maxIqrMillis) && input.getClickCount() >= 6;
        // result: smaller of normalized triggers (use jitter as primary value)
        return new SimpleCheckResult<>(flagged, jitter);
    }
}
