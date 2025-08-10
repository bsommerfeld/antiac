package de.bsommerfeld.antiac.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import de.bsommerfeld.antiac.AntiAC;
import de.bsommerfeld.antiac.AntiACConfig;
import de.bsommerfeld.antiac.capture.ClickCollector;
import de.bsommerfeld.antiac.command.AntiACCommand;
import de.bsommerfeld.antiac.detection.CheckManager;
import de.bsommerfeld.antiac.detection.checks.HighCpsCheck;
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
    double threshold = config.getHighCpsThreshold();
    return new CheckManager().add(new HighCpsCheck(threshold));
  }
}
