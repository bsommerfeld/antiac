package de.bsommerfeld.antiac.detection.checks;

import de.bsommerfeld.antiac.detection.Check;
import de.bsommerfeld.antiac.detection.CheckResult;
import de.bsommerfeld.antiac.detection.SimpleCheckResult;
import de.bsommerfeld.antiac.detection.features.FeatureVector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains a simple per-player suspicion level based on multiple indicators and flags when it exceeds a threshold.
 * Levels increase when suspicious patterns are observed and decay otherwise.
 */
public final class LevelEscalationCheck implements Check<FeatureVector, Integer> {
    private final Map<UUID, Integer> levels = new ConcurrentHashMap<>();

    private final int flagLevel;
    private final int increaseOnHit;
    private final int decayOnMiss;

    // Indicators
    private final double minCps;
    private final int minDoubleClicks;
    private final double maxUniformJitter;
    private final double minSteadyAimRatio;

    public LevelEscalationCheck(int flagLevel, int increaseOnHit, int decayOnMiss,
                                double minCps, int minDoubleClicks, double maxUniformJitter, double minSteadyAimRatio) {
        this.flagLevel = flagLevel;
        this.increaseOnHit = increaseOnHit;
        this.decayOnMiss = decayOnMiss;
        this.minCps = minCps;
        this.minDoubleClicks = minDoubleClicks;
        this.maxUniformJitter = maxUniformJitter;
        this.minSteadyAimRatio = minSteadyAimRatio;
    }

    @Override
    public CheckResult<Integer> execute(FeatureVector fv) {
        UUID id = fv.getPlayerId();
        int level = levels.getOrDefault(id, 0);
        boolean suspicious = false;

        if (fv.getCps() >= minCps) suspicious = true;
        if (fv.getDoubleClickCount() >= minDoubleClicks) suspicious = true;
        if (fv.getJitter() <= maxUniformJitter && fv.getClickCount() >= 6) suspicious = true;
        if (fv.getSteadyAimRatio() >= minSteadyAimRatio && fv.getClickCount() >= 6) suspicious = true;

        if (suspicious) level += increaseOnHit; else level = Math.max(0, level - decayOnMiss);
        levels.put(id, level);

        boolean flagged = level >= flagLevel;
        return new SimpleCheckResult<>(flagged, level);
    }
}
