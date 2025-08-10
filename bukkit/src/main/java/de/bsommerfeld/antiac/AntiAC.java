package de.bsommerfeld.antiac;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.TimeStampMode;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.bsommerfeld.antiac.bstats.BStatsHandler;
import de.bsommerfeld.antiac.guice.AntiACModule;
import de.bsommerfeld.antiac.logging.LogManager;
import de.bsommerfeld.antiac.messages.Messages;
import de.bsommerfeld.antiac.listener.ClickListener;
import de.bsommerfeld.antiac.service.DetectionService;
import de.bsommerfeld.antiac.command.AntiACCommand;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for AntiAC.
 */
public final class AntiAC extends JavaPlugin {

  private static AntiAC instance;
  private static Injector injector;
  private boolean debugEnabled = false;
  private AntiACConfig config;

  /**
   * Gets the singleton instance of the plugin.
   *
   * @return The plugin instance
   */
  public static AntiAC getInstance() {
    return instance;
  }

  /**
   * Gets the Guice injector for the plugin.
   *
   * @return The Guice injector
   */
  public static Injector getInjector() {
    return injector;
  }

  @Override
  public void onLoad() {
    // Set instance early for logging
    instance = this;

    // Load AntiAC configuration
    this.config = AntiACConfig.load(getDataFolder());
    this.debugEnabled = config.isDebug();

    // Create Guice injector and initialize logging
    createGuiceInjector();

    // Initialize packet events
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    initialize();
    setupPacketEvents();
    initializeBStats();

    // Register PacketEvents listener
    ClickListener clickListener = injector.getInstance(ClickListener.class);
    PacketEvents.getAPI().getEventManager().registerListener(clickListener);

    // Register /antiac command executor & tab completer
    AntiACCommand antiACCommand = injector.getInstance(AntiACCommand.class);
    if (getCommand("antiac") != null) {
      getCommand("antiac").setExecutor(antiACCommand);
      getCommand("antiac").setTabCompleter(antiACCommand);
    } else {
      LogManager.warning("Command 'antiac' not found in plugin.yml");
    }

    // Start detection sampling
    DetectionService detectionService = injector.getInstance(DetectionService.class);
    long periodTicks = config.getSamplePeriodTicks();
    detectionService.start(periodTicks);

    LogManager.info("AntiAC has been enabled successfully");
  }

  @Override
  public void onDisable() {
    try {
      DetectionService detectionService = injector.getInstance(DetectionService.class);
      detectionService.stop();
    } catch (Exception ignored) {}
    try {
      PacketEvents.getAPI().terminate();
    } catch (Exception ignored) {}
    LogManager.info("AntiAC has been disabled");
  }

  private void initialize() {
    setupMessages();
  }

  private void createGuiceInjector() {
    injector = Guice.createInjector(new AntiACModule(this, config));
    LogManager.initialize(injector);
    LogManager.debug("Guice injector created with debug mode: " + debugEnabled);
  }

  private void setupMessages() {
    Messages.setup();
    Messages.migrate();
  }

  private void initializeBStats() {
    BStatsHandler.init(this);
  }

  private void setupPacketEvents() {
    PacketEvents.getAPI()
        .getSettings()
        .debug(debugEnabled)
        .checkForUpdates(false)
        .timeStampMode(TimeStampMode.MILLIS)
        .bStats(false)
        .reEncodeByDefault(true);
    PacketEvents.getAPI().init();
  }
}
