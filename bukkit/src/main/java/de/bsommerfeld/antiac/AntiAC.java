package de.bsommerfeld.antiac;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.TimeStampMode;
import com.google.inject.Guice;
import de.bsommerfeld.antiac.bstats.BStatsHandler;
import de.bsommerfeld.antiac.guice.AntiACModule;
import de.bsommerfeld.antiac.messages.Messages;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiAC extends JavaPlugin {

  private static AntiAC instance;

  public static AntiAC getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    createGuiceInjector();
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    instance = this;
    initialize();
    setupPacketEvents();
    initializeBStats();
  }

  @Override
  public void onDisable() {}

  private void initialize() {
    setupMessages();
  }

  private void createGuiceInjector() {
    Guice.createInjector(new AntiACModule());
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
        .debug(false)
        .checkForUpdates(false)
        .timeStampMode(TimeStampMode.MILLIS)
        .bStats(false)
        .reEncodeByDefault(true);
    PacketEvents.getAPI().init();
  }
}
