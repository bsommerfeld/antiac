package de.bsommerfeld.antiac.detection.service;

import de.bsommerfeld.antiac.AntiAC;
import de.bsommerfeld.antiac.bstats.BStatsHandler;
import de.bsommerfeld.antiac.click.CPS;
import de.bsommerfeld.antiac.click.Click;
import de.bsommerfeld.antiac.click.ClickTracker;
import de.bsommerfeld.antiac.click.ClickType;
import de.bsommerfeld.antiac.detection.Check;
import de.bsommerfeld.antiac.event.PlayerFlaggedEvent;
import de.bsommerfeld.antiac.hibernate.entity.LogEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PlayerFlaggingService {

  private final ClickTracker clickTracker;
  private final Check check;

  public void flagPlayer(Player player) {
    BStatsHandler.increaseFlagged();
    Bukkit.getScheduler()
        .runTask(
            AntiAC.getInstance(),
            () ->
                Bukkit.getPluginManager()
                    .callEvent(
                        new PlayerFlaggedEvent(
                            player, clickTracker.getCPSList(player.getUniqueId()), check)));

    handleFlag(player);

    if (isLoggingActivated()) logFlag(player);
  }

  public void handleFlag(Player player) {
    List<String> commands = AntiAC.getInstance().getConfiguration().getCommands();
    if (commands.isEmpty()) return;
    dispatchCommands(player, commands);
  }

  private void dispatchCommands(Player player, List<String> commands) {
    commands.forEach(
        command -> {
          String finalCommand = replacePlaceholder(player, command);
          Bukkit.getScheduler()
              .runTask(
                  AntiAC.getInstance(),
                  () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand));
        });
  }

  private @NotNull String replacePlaceholder(Player player, String command) {
    command = command.replace("%player%", player.getName());
    command = command.replace("%check%", check.getName());
    return command;
  }

  private boolean isLoggingActivated() {
    return AntiAC.getInstance().getConfiguration().isLogging();
  }

  private void logFlag(Player player) {
    LogEntry logEntry = craftLogEntry(player);
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AntiAC.getInstance(),
            () -> AntiAC.getInstance().getLogEntryRepository().save(logEntry));
  }

  private @NotNull LogEntry craftLogEntry(Player player) {
    UUID playerUuid = player.getUniqueId();
    String checkName = check.getName();
    CPS latestCPS = clickTracker.getLatestCPS(playerUuid);
    return new LogEntry(playerUuid, checkName, latestCPS.getCPS(), getAverageClickType(latestCPS));
  }

  private ClickType getAverageClickType(CPS cps) {
    List<ClickType> clickTypes = new ArrayList<>();
    cps.getClicks().stream().map(Click::getClickType).forEach(clickTypes::add);

    return clickTypes.stream()
        .max((clickType1, clickType2) -> clickTypeComparison(clickType1, clickType2, clickTypes))
        .orElse(null);
  }

  private static int clickTypeComparison(
      ClickType clickType1, ClickType clickType2, List<ClickType> clickTypes) {
    int count1 = (int) clickTypes.stream().filter(type -> type == clickType1).count();
    int count2 = (int) clickTypes.stream().filter(type -> type == clickType2).count();
    return Integer.compare(count1, count2);
  }
}
