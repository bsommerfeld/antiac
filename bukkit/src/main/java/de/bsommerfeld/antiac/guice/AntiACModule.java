package de.bsommerfeld.antiac.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import de.bsommerfeld.antiac.AntiAC;
import de.bsommerfeld.antiac.AntiACConfig;
import de.bsommerfeld.antiac.capture.ClickCollector;
import de.bsommerfeld.antiac.command.AntiACCommand;
import de.bsommerfeld.antiac.detection.CheckManager;
import de.bsommerfeld.antiac.detection.checks.CrosshairSteadinessCheck;
import de.bsommerfeld.antiac.detection.checks.DoubleClickCheck;
import de.bsommerfeld.antiac.detection.checks.HighCpsCheck;
import de.bsommerfeld.antiac.detection.checks.IntervalUniformityCheck;
import de.bsommerfeld.antiac.detection.checks.LevelEscalationCheck;
import de.bsommerfeld.antiac.detection.checks.MomentumCheck;
import de.bsommerfeld.antiac.detection.features.FeatureExtractor;
import de.bsommerfeld.antiac.feature.BasicFeatureExtractor;
import de.bsommerfeld.antiac.listener.ClickListener;
import de.bsommerfeld.antiac.logging.LoggingModule;
import de.bsommerfeld.antiac.service.DetectionService;

/**
 * Main Guice module for the AntiAC plugin.
 * Configures all dependencies for the application.
 */
public class AntiACModule extends AbstractModule {

  private final AntiAC plugin;
  private final AntiACConfig config;
  private final boolean debugEnabled;

  /**
   * Creates a new AntiACModule.
   *
   * @param plugin The AntiAC plugin instance
   * @param config The AntiAC configuration instance
   */
  public AntiACModule(AntiAC plugin, AntiACConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.debugEnabled = config.isDebug();
  }

  @Override
  protected void configure() {
    // Install other modules
    install(new LoggingModule(plugin, debugEnabled));

    // Bind interfaces to implementations
    bind(FeatureExtractor.class).to(BasicFeatureExtractor.class).in(Singleton.class);

    // Ensure listener and services exist as singletons
    bind(ClickListener.class).in(Singleton.class);
    bind(DetectionService.class).in(Singleton.class);
    bind(AntiACCommand.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  AntiACConfig provideAntiACConfig() {
    return config;
  }

  @Provides
  @Singleton
  ClickCollector provideClickCollector() {
    long windowMillis = config.getWindowMillis();
    return new ClickCollector(windowMillis);
  }

  @Provides
  @Singleton
  CheckManager provideCheckManager() {
    double highCps = config.getHighCpsThreshold();
    // Sensible defaults; could be moved to config in future
    DoubleClickCheck doubleClick = new DoubleClickCheck(3);
    MomentumCheck momentum = new MomentumCheck(10.0, 0.2, 4.0);
    CrosshairSteadinessCheck steady = new CrosshairSteadinessCheck(10.0, 0.7);
    IntervalUniformityCheck uniform = new IntervalUniformityCheck(10.0, 0.15, 12.0);
    LevelEscalationCheck levels = new LevelEscalationCheck(6, 2, 1, 15.0, 3, 0.15, 0.7);

    return new CheckManager()
        .add(new HighCpsCheck(highCps))
        .add(doubleClick)
        .add(momentum)
        .add(steady)
        .add(uniform)
        .add(levels);
  }
}
