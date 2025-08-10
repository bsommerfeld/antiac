package de.bsommerfeld.antiac.detection.checks;

import de.bsommerfeld.antiac.detection.Check;
import de.bsommerfeld.antiac.detection.CheckResult;
import de.bsommerfeld.antiac.detection.SimpleCheckResult;
import de.bsommerfeld.antiac.detection.features.FeatureVector;

/**
 * Flags when CPS exceeds a configured threshold.
 * The result contains the observed CPS.
 */
public final class HighCpsCheck implements Check<FeatureVector, Double> {
    private final double cpsThreshold;

    public HighCpsCheck(double cpsThreshold) {
        this.cpsThreshold = cpsThreshold;
    }

    @Override
    public CheckResult<Double> execute(FeatureVector input) {
        double cps = input.getCps();
        boolean flagged = cps >= cpsThreshold;
        return new SimpleCheckResult<>(flagged, cps);
    }

    public double getCpsThreshold() {
        return cpsThreshold;
    }
}
