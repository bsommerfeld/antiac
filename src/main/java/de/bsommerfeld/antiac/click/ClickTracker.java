package de.bsommerfeld.antiac.click;

import de.bsommerfeld.antiac.AntiACConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClickTracker {

  private final Map<UUID, List<CPS>> playerClicksMap = new HashMap<>();
  private final AntiACConfig configuration;

  public synchronized void addClick(UUID player, Click click) {
    ensurePlayerExists(player);
    CPS currentCPS = getLatestCPS(player);
    currentCPS.addClick(click);
  }

  public synchronized void addNewCPS(UUID player) {
    ensurePlayerExists(player);
    playerClicksMap.get(player).add(new CPS());
  }

  public synchronized void removePlayer(UUID player) {
    playerClicksMap.remove(player);
  }

  private void ensurePlayerExists(UUID player) {
    playerClicksMap.computeIfAbsent(player, k -> new ArrayList<>());
  }

  public synchronized List<CPS> getCPSList(UUID player) {
    return playerClicksMap.getOrDefault(player, new ArrayList<>());
  }

  public synchronized CPS getLatestCPS(UUID player) {
    List<CPS> cpsList = playerClicksMap.get(player);
    return (cpsList == null || cpsList.isEmpty()) ? CPS.EMPTY : cpsList.get(cpsList.size() - 1);
  }

  public void removeLastCPSIfExceedsLimit() {
    int maxCPS = configuration.getCpsStorageLimit();
    playerClicksMap
        .values()
        .forEach(
            cpsList -> {
              if (cpsList.size() > maxCPS) {
                cpsList.removeFirst();
              }
            });
  }
}
