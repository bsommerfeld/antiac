package de.bsommerfeld.antiac.bstats;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

public class BStatsHandler {

  private static int flagged;

  public static void init(JavaPlugin javaPlugin) {
    Metrics metrics = new Metrics(javaPlugin, 26162);
    metrics.addCustomChart(
        new SingleLineChart(
            "flagged_players",
            () -> {
              int totalFlagged = flagged;
              flagged = 0;
              return totalFlagged;
            }));
  }

  public static synchronized void increaseFlagged() {
    flagged++;
  }
}
