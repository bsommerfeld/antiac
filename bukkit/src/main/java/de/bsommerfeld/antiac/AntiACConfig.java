package de.bsommerfeld.antiac;

import de.bsommerfeld.jshepherd.annotation.Key;
import de.bsommerfeld.jshepherd.core.ConfigurablePojo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Central configuration POJO for AntiAC loaded from antiac.yml
 */
public class AntiACConfig extends ConfigurablePojo<AntiACConfig> {

  // General
  @Key("debug")
  private boolean debug = false;

  // Detection
  @Key("detection.windowMillis")
  private long windowMillis = 5_000L;

  @Key("detection.samplePeriodTicks")
  private long samplePeriodTicks = 20L;

  @Key("detection.highCpsThreshold")
  private double highCpsThreshold = 18.0;

  // Monitor (command) settings
  @Key("monitor.durationSeconds")
  private int monitorDurationSeconds = 10;

  // Database (reserved for future use)
  @Key("database-url")
  private String databaseUrl = "";

  @Key("database-username")
  private String databaseUsername = "";

  @Key("database-password")
  private String databasePassword = "";

  public boolean isDebug() { return debug; }
  public long getWindowMillis() { return windowMillis; }
  public long getSamplePeriodTicks() { return samplePeriodTicks; }
  public double getHighCpsThreshold() { return highCpsThreshold; }
  public int getMonitorDurationSeconds() { return monitorDurationSeconds; }

  public String getDatabaseUrl() { return databaseUrl; }
  public String getDatabaseUsername() { return databaseUsername; }
  public String getDatabasePassword() { return databasePassword; }

  // Loader
  public static AntiACConfig load(File dataFolder) {
    if (!dataFolder.exists()) {
      // noinspection ResultOfMethodCallIgnored
      dataFolder.mkdirs();
    }
    File file = new File(dataFolder, "antiac.yml");
    if (!file.exists()) {
      AntiACConfig defaults = new AntiACConfig();
      saveToFile(defaults, file);
      return defaults;
    }
    FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
    AntiACConfig cfg = new AntiACConfig();
    cfg.debug = yaml.getBoolean("debug", cfg.debug);
    cfg.windowMillis = yaml.getLong("detection.windowMillis", cfg.windowMillis);
    cfg.samplePeriodTicks = yaml.getLong("detection.samplePeriodTicks", cfg.samplePeriodTicks);
    cfg.highCpsThreshold = yaml.getDouble("detection.highCpsThreshold", cfg.highCpsThreshold);
    cfg.monitorDurationSeconds = yaml.getInt("monitor.durationSeconds", cfg.monitorDurationSeconds);
    cfg.databaseUrl = yaml.getString("database-url", cfg.databaseUrl);
    cfg.databaseUsername = yaml.getString("database-username", cfg.databaseUsername);
    cfg.databasePassword = yaml.getString("database-password", cfg.databasePassword);
    // Ensure file contains all keys (write back if missing)
    saveToFile(cfg, file);
    return cfg;
  }

  private static void saveToFile(AntiACConfig cfg, File file) {
    FileConfiguration yaml = new YamlConfiguration();
    yaml.set("debug", cfg.debug);
    yaml.set("detection.windowMillis", cfg.windowMillis);
    yaml.set("detection.samplePeriodTicks", cfg.samplePeriodTicks);
    yaml.set("detection.highCpsThreshold", cfg.highCpsThreshold);
    yaml.set("monitor.durationSeconds", cfg.monitorDurationSeconds);
    yaml.set("database-url", cfg.databaseUrl);
    yaml.set("database-username", cfg.databaseUsername);
    yaml.set("database-password", cfg.databasePassword);
    try {
      yaml.save(file);
    } catch (IOException ignored) {
    }
  }
}
