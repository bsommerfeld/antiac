package de.bsommerfeld.antiac;

import co.aikar.commands.PaperCommandManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.util.TimeStampMode;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import de.bsommerfeld.antiac.bstats.BStatsHandler;
import de.bsommerfeld.antiac.click.ClickTracker;
import de.bsommerfeld.antiac.commands.AntiACCommand;
import de.bsommerfeld.antiac.detection.Check;
import de.bsommerfeld.antiac.detection.CheckRegistry;
import de.bsommerfeld.antiac.detection.checks.AFKClickingCheck;
import de.bsommerfeld.antiac.detection.checks.ClickLimitCheck;
import de.bsommerfeld.antiac.detection.checks.ClickPatternConsistencyCheck;
import de.bsommerfeld.antiac.detection.checks.DoubleClickCheck;
import de.bsommerfeld.antiac.detection.checks.MomentumCheck;
import de.bsommerfeld.antiac.detection.checks.ScaledCPSCheck;
import de.godcipher.antiac.detection.checks.*;
import de.bsommerfeld.antiac.detection.reliability.TPSChecker;
import de.bsommerfeld.antiac.detection.violation.ViolationTracker;
import de.bsommerfeld.antiac.hibernate.HibernateUtil;
import de.bsommerfeld.antiac.hibernate.repository.impl.LogEntryRepositoryImpl;
import de.bsommerfeld.antiac.listener.bukkit.PlayerQuitListener;
import de.bsommerfeld.antiac.listener.protocol.PlayerAttackEntityPacketListener;
import de.bsommerfeld.antiac.messages.Messages;
import de.bsommerfeld.antiac.tasks.CheckExecutionTask;
import de.bsommerfeld.antiac.tasks.ClearViolationsTask;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j
public final class AntiAC extends JavaPlugin {

  private static final String SPIGOT_RESOURCE_ID = "74933";

  @Getter private static AntiAC instance;

  @Getter private final AntiACConfig configuration = new AntiACConfig(); // Use AntiACConfig

  @Getter private final LogEntryRepositoryImpl logEntryRepository = new LogEntryRepositoryImpl();

  private final TPSChecker tpsChecker = new TPSChecker(configuration);
  @Getter private final ClickTracker clickTracker = new ClickTracker(configuration);
  @Getter private final ViolationTracker violationTracker = new ViolationTracker();

  @Getter
  private final CheckRegistry checkRegistry = new CheckRegistry(violationTracker, configuration);

  @Override
  public void onLoad() {
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    instance = this;
    initialize();
    setupHibernate();
    setupCommandFramework();
    setupPacketEvents();
    initializeBStats();
    registerBukkitListener();
    registerPacketListener();
  }

  @Override
  public void onDisable() {
    shutdown();
  }

  private void initialize() {
    configuration.setDirectory(getDataFolder());
    configuration.initialize();
    registerChecks();
    setupMessages();
    startTPSChecker();
    runTasks();
    printRegisteredChecksAmount();
    runUpdateChecker();
    logEntryRepository.startCacheUpdater();
  }

  private void shutdown() {
    logEntryRepository.shutdownCacheUpdater();
    HibernateUtil.shutdown();
  }

  private void runUpdateChecker() {
    new UpdateChecker(this, UpdateCheckSource.SPIGOT, SPIGOT_RESOURCE_ID)
        .checkEveryXHours(12)
        .setColoredConsoleOutput(true)
        .checkNow();
  }

  private void setupHibernate() {
    if (configuration.isLogging()) HibernateUtil.setupHibernate();
  }

  private void setupMessages() {
    Messages.setup();
    Messages.migrate();
  }

  private void setupCommandFramework() {
    PaperCommandManager commandManager = new PaperCommandManager(this);
    registerCommandCompletions(commandManager);
    commandManager.registerCommand(
        new AntiACCommand(clickTracker, violationTracker, logEntryRepository, configuration));
  }

  private void registerCommandCompletions(PaperCommandManager commandManager) {
    commandManager
        .getCommandCompletions()
        .registerCompletion(
            "checks",
            completionHandler ->
                checkRegistry.getChecks().stream()
                    .map(Check::getName)
                    .collect(Collectors.toList()));
  }

  private void initializeBStats() {
    BStatsHandler.init(this);
  }

  private void startTPSChecker() {
    tpsChecker.start();
  }

  private void printRegisteredChecksAmount() {
    log.info("Registered {} checks", checkRegistry.getChecks().size());
  }

  private void runTasks() {
    getServer()
        .getScheduler()
        .runTaskTimerAsynchronously(
            this, new CheckExecutionTask(clickTracker, checkRegistry, tpsChecker), 0, 20);
    getServer()
        .getScheduler()
        .runTaskTimerAsynchronously(
            this, new ClearViolationsTask(violationTracker), 0, 20 * 60); // 1 minute
  }

  private void registerChecks() {
    checkRegistry.registerCheck(new AFKClickingCheck(clickTracker));
    checkRegistry.registerCheck(new ClickPatternConsistencyCheck(clickTracker));
    checkRegistry.registerCheck(new ClickLimitCheck(clickTracker));
    checkRegistry.registerCheck(new DoubleClickCheck(clickTracker));
    checkRegistry.registerCheck(new MomentumCheck(clickTracker));
    checkRegistry.registerCheck(new ScaledCPSCheck(clickTracker));
  }

  private void registerBukkitListener() {
    getServer()
        .getPluginManager()
        .registerEvents(new PlayerQuitListener(clickTracker, checkRegistry), this);
  }

  private void setupPacketEvents() {
    PacketEvents.getAPI()
        .getSettings()
        .debug(false)
        .checkForUpdates(false)
        .timeStampMode(TimeStampMode.MILLIS)
        .reEncodeByDefault(true);
    PacketEvents.getAPI().init();
  }

  private void registerPacketListener() {
    EventManager eventManager = PacketEvents.getAPI().getEventManager();
    eventManager.registerListener(
        new PlayerAttackEntityPacketListener(clickTracker), PacketListenerPriority.NORMAL);
  }
}
