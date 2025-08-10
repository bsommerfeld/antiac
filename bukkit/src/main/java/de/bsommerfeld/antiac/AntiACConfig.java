package de.bsommerfeld.antiac;

import de.bsommerfeld.jshepherd.annotation.Comment;
import de.bsommerfeld.jshepherd.annotation.Key;
import de.bsommerfeld.jshepherd.annotation.PostInject;
import de.bsommerfeld.jshepherd.core.ConfigurablePojo;
import de.bsommerfeld.jshepherd.core.ConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

/**
 * Central configuration POJO for AntiAC loaded from antiac.yml
 */
public class AntiACConfig extends ConfigurablePojo<AntiACConfig> {

    // General
    @Comment("Enable verbose debug logging in console and log files")
    @Key("debug")
    private boolean debug = false;

    // Detection window & scheduling
    @Comment("Size of the rolling time window (milliseconds) used for click analysis")
    @Key("detection.windowMillis")
    private long windowMillis = 5_000L;

    @Comment("How often features are sampled and checks are executed (ticks). 20 ticks = 1 second")
    @Key("detection.samplePeriodTicks")
    private long samplePeriodTicks = 20L;

    // High CPS check
    @Comment("Flag when measured CPS equals or exceeds this threshold")
    @Key("checks.highCps.threshold")
    private double highCpsThreshold = 18.0;

    // Double click check
    @Comment("Minimum number of detected double-clicks within the window to flag")
    @Key("checks.doubleClick.minCount")
    private int doubleClickMinCount = 3;

    // Momentum check
    @Comment("Minimum CPS required before momentum stability/spike is evaluated")
    @Key("checks.momentum.minCps")
    private double momentumMinCps = 10.0;

    @Comment("Delta CPS threshold to consider the window suspiciously stable (abs delta <= threshold)")
    @Key("checks.momentum.stableDeltaThreshold")
    private double momentumStableDeltaThreshold = 0.2;

    @Comment("Delta CPS threshold to consider the window a suspicious spike (abs delta >= threshold)")
    @Key("checks.momentum.spikeDeltaThreshold")
    private double momentumSpikeDeltaThreshold = 4.0;

    // Crosshair steadiness check
    @Comment("Minimum CPS required before crosshair steadiness is considered")
    @Key("checks.crosshairSteady.minCps")
    private double steadyMinCps = 10.0;

    @Comment("Minimum steady-aim ratio (0.0 - 1.0) to flag. Higher = less movement allowed")
    @Key("checks.crosshairSteady.minSteadyRatio")
    private double steadyMinRatio = 0.7;

    // Interval uniformity check
    @Comment("Minimum CPS required before interval uniformity (jitter/IQR) is considered")
    @Key("checks.uniformity.minCps")
    private double uniformMinCps = 10.0;

    @Comment("Maximum jitter coefficient of variation (CV) allowed. Lower = more uniform (more suspicious)")
    @Key("checks.uniformity.maxJitterCv")
    private double uniformMaxJitterCv = 0.15;

    @Comment("Maximum interquartile range (IQR) of inter-click intervals in milliseconds")
    @Key("checks.uniformity.maxIqrMillis")
    private double uniformMaxIqrMillis = 12.0;

    // Level escalation check
    @Comment("Suspicion level at or above which a player is flagged")
    @Key("checks.escalation.flagLevel")
    private int escalationFlagLevel = 6;

    @Comment("Amount to increase the suspicion level when suspicious indicators are present")
    @Key("checks.escalation.increaseOnHit")
    private int escalationIncreaseOnHit = 2;

    @Comment("Amount to decrease the suspicion level when indicators are not present")
    @Key("checks.escalation.decayOnMiss")
    private int escalationDecayOnMiss = 1;

    @Comment("Indicator: minimum CPS considered as suspicious")
    @Key("checks.escalation.indicator.minCps")
    private double escalationMinCps = 15.0;

    @Comment("Indicator: minimum double-click count considered as suspicious")
    @Key("checks.escalation.indicator.minDoubleClicks")
    private int escalationMinDoubleClicks = 3;

    @Comment("Indicator: maximum jitter CV considered uniformly suspicious")
    @Key("checks.escalation.indicator.maxUniformJitter")
    private double escalationMaxUniformJitter = 0.15;

    @Comment("Indicator: minimum steady-aim ratio considered suspicious")
    @Key("checks.escalation.indicator.minSteadyAimRatio")
    private double escalationMinSteadyAim = 0.7;

    // Monitor (command) settings
    @Comment("Default duration (seconds) for the /antiac monitor command")
    @Key("monitor.durationSeconds")
    private int monitorDurationSeconds = 10;

    // Database (reserved for future use)
    @Comment("JDBC database URL (reserved for future use); leave blank to disable")
    @Key("database-url")
    private String databaseUrl = "";

    @Comment("Database username (reserved for future use)")
    @Key("database-username")
    private String databaseUsername = "";

    @Comment("Database password (reserved for future use)")
    @Key("database-password")
    private String databasePassword = "";

    // Getters
    public boolean isDebug() { return debug; }
    public long getWindowMillis() { return windowMillis; }
    public long getSamplePeriodTicks() { return samplePeriodTicks; }

    public double getHighCpsThreshold() { return highCpsThreshold; }

    public int getDoubleClickMinCount() { return doubleClickMinCount; }

    public double getMomentumMinCps() { return momentumMinCps; }
    public double getMomentumStableDeltaThreshold() { return momentumStableDeltaThreshold; }
    public double getMomentumSpikeDeltaThreshold() { return momentumSpikeDeltaThreshold; }

    public double getSteadyMinCps() { return steadyMinCps; }
    public double getSteadyMinRatio() { return steadyMinRatio; }

    public double getUniformMinCps() { return uniformMinCps; }
    public double getUniformMaxJitterCv() { return uniformMaxJitterCv; }
    public double getUniformMaxIqrMillis() { return uniformMaxIqrMillis; }

    public int getEscalationFlagLevel() { return escalationFlagLevel; }
    public int getEscalationIncreaseOnHit() { return escalationIncreaseOnHit; }
    public int getEscalationDecayOnMiss() { return escalationDecayOnMiss; }

    public double getEscalationMinCps() { return escalationMinCps; }
    public int getEscalationMinDoubleClicks() { return escalationMinDoubleClicks; }
    public double getEscalationMaxUniformJitter() { return escalationMaxUniformJitter; }
    public double getEscalationMinSteadyAim() { return escalationMinSteadyAim; }

    public int getMonitorDurationSeconds() { return monitorDurationSeconds; }

    public String getDatabaseUrl() { return databaseUrl; }
    public String getDatabaseUsername() { return databaseUsername; }
    public String getDatabasePassword() { return databasePassword; }

    @PostInject
    private void validate() {
        if (windowMillis <= 0) throw new IllegalArgumentException("detection.windowMillis must be > 0");
        if (samplePeriodTicks <= 0) throw new IllegalArgumentException("detection.samplePeriodTicks must be > 0");
        if (highCpsThreshold < 0) throw new IllegalArgumentException("checks.highCps.threshold must be >= 0");
        if (doubleClickMinCount < 0) throw new IllegalArgumentException("checks.doubleClick.minCount must be >= 0");
        if (momentumMinCps < 0) throw new IllegalArgumentException("checks.momentum.minCps must be >= 0");
        if (momentumStableDeltaThreshold < 0) throw new IllegalArgumentException("checks.momentum.stableDeltaThreshold must be >= 0");
        if (momentumSpikeDeltaThreshold < 0) throw new IllegalArgumentException("checks.momentum.spikeDeltaThreshold must be >= 0");
        if (steadyMinCps < 0) throw new IllegalArgumentException("checks.crosshairSteady.minCps must be >= 0");
        if (steadyMinRatio < 0 || steadyMinRatio > 1) throw new IllegalArgumentException("checks.crosshairSteady.minSteadyRatio must be between 0 and 1");
        if (uniformMinCps < 0) throw new IllegalArgumentException("checks.uniformity.minCps must be >= 0");
        if (uniformMaxJitterCv < 0) throw new IllegalArgumentException("checks.uniformity.maxJitterCv must be >= 0");
        if (uniformMaxIqrMillis < 0) throw new IllegalArgumentException("checks.uniformity.maxIqrMillis must be >= 0");
        if (escalationFlagLevel < 0) throw new IllegalArgumentException("checks.escalation.flagLevel must be >= 0");
        if (escalationIncreaseOnHit < 0) throw new IllegalArgumentException("checks.escalation.increaseOnHit must be >= 0");
        if (escalationDecayOnMiss < 0) throw new IllegalArgumentException("checks.escalation.decayOnMiss must be >= 0");
        if (escalationMinCps < 0) throw new IllegalArgumentException("checks.escalation.indicator.minCps must be >= 0");
        if (escalationMinDoubleClicks < 0) throw new IllegalArgumentException("checks.escalation.indicator.minDoubleClicks must be >= 0");
        if (escalationMaxUniformJitter < 0) throw new IllegalArgumentException("checks.escalation.indicator.maxUniformJitter must be >= 0");
        if (escalationMinSteadyAim < 0 || escalationMinSteadyAim > 1) throw new IllegalArgumentException("checks.escalation.indicator.minSteadyAimRatio must be between 0 and 1");
    }

    /**
     * Loads the configuration from antiac.yml in the given data folder, creating it with defaults if necessary.
     */
    public static AntiACConfig load(File dataFolder) {
        if (!dataFolder.exists()) dataFolder.mkdirs();
        Path path = new File(dataFolder, "antiac.yml").toPath();
        return ConfigurationLoader.load(path, AntiACConfig::new);
    }
}
