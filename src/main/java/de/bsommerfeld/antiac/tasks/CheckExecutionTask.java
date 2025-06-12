package de.bsommerfeld.antiac.tasks;

import de.bsommerfeld.antiac.click.CPS;
import de.bsommerfeld.antiac.click.ClickTracker;
import de.bsommerfeld.antiac.detection.CheckRegistry;
import de.bsommerfeld.antiac.detection.reliability.TPSChecker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Slf4j
public class CheckExecutionTask implements Runnable {

  private final ClickTracker clickTracker;
  private final CheckRegistry checkRegistry;
  private final TPSChecker tpsChecker;

  @Override
  public void run() {
    if (!tpsChecker.isReliable()) {
      log.debug("TPS is not reliable, skipping check execution");
      return;
    }
    processOnlinePlayers();
    cleanupOldCPS();
  }

  private void processOnlinePlayers() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      processPlayer(player);
    }
  }

  private void processPlayer(Player player) {
    UUID playerId = player.getUniqueId();
    ensureFirstCPS(playerId);
    checkRegistry.performChecks(player);
    clickTracker.addNewCPS(playerId);
  }

  private void ensureFirstCPS(UUID playerId) {
    if (clickTracker.getLatestCPS(playerId) == CPS.EMPTY) {
      clickTracker.addNewCPS(playerId);
    }
  }

  private void cleanupOldCPS() {
    clickTracker.removeLastCPSIfExceedsLimit();
  }
}
