package de.bsommerfeld.antiac.detection.checks;

import de.bsommerfeld.antiac.detection.Check;
import de.bsommerfeld.antiac.detection.CheckResult;
import de.bsommerfeld.antiac.detection.SimpleCheckResult;
import de.bsommerfeld.antiac.detection.features.FeatureVector;

/**
 * Flags when too many short inter-click intervals ("double clicks") are observed.
 */
public final class DoubleClickCheck implements Check<FeatureVector, Integer> {
    private final int minDoubleClicksToFlag;

    public DoubleClickCheck(int minDoubleClicksToFlag) {
        this.minDoubleClicksToFlag = minDoubleClicksToFlag;
    }

    @Override
    public CheckResult<Integer> execute(FeatureVector input) {
        int count = input.getDoubleClickCount();
        boolean flagged = count >= minDoubleClicksToFlag && input.getClickCount() >= 6;
        return new SimpleCheckResult<>(flagged, count);
    }
}
