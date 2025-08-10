package de.bsommerfeld.antiac.detection;

import de.bsommerfeld.antiac.detection.features.FeatureVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Manages a set of checks that operate on FeatureVector inputs.
 */
public final class CheckManager {
    private final List<Check<FeatureVector, ?>> checks = new ArrayList<>();

    public CheckManager() {}

    public CheckManager add(Check<FeatureVector, ?> check) {
        checks.add(Objects.requireNonNull(check));
        return this;
    }

    public List<Check<FeatureVector, ?>> getChecks() {
        return Collections.unmodifiableList(checks);
    }

    public List<CheckResult<?>> runAll(FeatureVector features) {
        List<CheckResult<?>> results = new ArrayList<>(checks.size());
        for (Check<FeatureVector, ?> check : checks) {
            results.add(check.execute(features));
        }
        return results;
    }
}
