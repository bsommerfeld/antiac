package de.bsommerfeld.antiac.detection.checks;

import de.bsommerfeld.antiac.detection.Check;
import de.bsommerfeld.antiac.detection.CheckResult;
import de.bsommerfeld.antiac.detection.SimpleCheckResult;
import de.bsommerfeld.antiac.detection.features.FeatureVector;

/**
 * Detects suspiciously stable or excessively spiking momentum in CPS within the window.
 *
 * Flags when:
 *  - high CPS with nearly no change between halves (absDelta <= stableDeltaThreshold), or
 *  - very large spike between halves (absDelta >= spikeDeltaThreshold)
 */
public final class MomentumCheck implements Check<FeatureVector, Double> {
    private final double minCpsToConsider;
    private final double stableDeltaThreshold;
    private final double spikeDeltaThreshold;

    public MomentumCheck(double minCpsToConsider, double stableDeltaThreshold, double spikeDeltaThreshold) {
        this.minCpsToConsider = minCpsToConsider;
        this.stableDeltaThreshold = stableDeltaThreshold;
        this.spikeDeltaThreshold = spikeDeltaThreshold;
    }

    @Override
    public CheckResult<Double> execute(FeatureVector fv) {
        double delta = fv.getCpsSecondHalf() - fv.getCpsFirstHalf();
        double cps = fv.getCps();
        boolean flagged = false;
        if (cps >= minCpsToConsider) {
            double abs = Math.abs(delta);
            flagged = abs <= stableDeltaThreshold || abs >= spikeDeltaThreshold;
        }
        return new SimpleCheckResult<>(flagged, delta);
    }
}
