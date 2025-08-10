package de.bsommerfeld.antiac.detection.checks;

import de.bsommerfeld.antiac.detection.Check;
import de.bsommerfeld.antiac.detection.CheckResult;
import de.bsommerfeld.antiac.detection.SimpleCheckResult;
import de.bsommerfeld.antiac.detection.features.FeatureVector;

/**
 * Flags when the crosshair barely moves between clicks while maintaining notable CPS.
 */
public final class CrosshairSteadinessCheck implements Check<FeatureVector, Double> {
    private final double minCpsToConsider;
    private final double minSteadyRatio;

    public CrosshairSteadinessCheck(double minCpsToConsider, double minSteadyRatio) {
        this.minCpsToConsider = minCpsToConsider;
        this.minSteadyRatio = minSteadyRatio;
    }

    @Override
    public CheckResult<Double> execute(FeatureVector fv) {
        double cps = fv.getCps();
        double steady = fv.getSteadyAimRatio();
        boolean flagged = cps >= minCpsToConsider && steady >= minSteadyRatio && fv.getClickCount() >= 6;
        return new SimpleCheckResult<>(flagged, steady);
    }
}
