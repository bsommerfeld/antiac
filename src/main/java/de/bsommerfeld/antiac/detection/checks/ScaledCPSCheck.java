package de.bsommerfeld.antiac.detection.checks;

import de.bsommerfeld.antiac.click.CPS;
import de.bsommerfeld.antiac.click.Click;
import de.bsommerfeld.antiac.click.ClickTracker;
import de.bsommerfeld.antiac.detection.Check;
import de.bsommerfeld.antiac.detection.checks.configs.ScaledCPSCheckConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * ScaledCPSCheck checks whether the player's click pattern exceeds the expected delay range when
 * scaled according to their clicks per second (CPS). If their actual click delay is too short for
 * the CPS, they are flagged as suspicious.
 */
@Slf4j
public class ScaledCPSCheck extends Check<ScaledCPSCheckConfig> {

  public ScaledCPSCheck(ClickTracker clickTracker) {
    super(clickTracker, new ScaledCPSCheckConfig());
  }

  @Override
  public boolean check(Player player) {
    CPS cps = clickTracker.getLatestCPS(player.getUniqueId());
    if (isInvalidClickData(cps)) return false;

    double actualTotalDelay = calculateTotalDelay(cps.getClicks());
    double expectedScaledDelay = scaleCPSDelay(cps.getCPS());

    if (actualTotalDelay < expectedScaledDelay) {
      log.debug(
          "Player: {} flagged for low total click delay. Actual: {}, Expected: {}",
          player.getName(),
          actualTotalDelay,
          expectedScaledDelay);
      onFlag(player);
      return true;
    }

    return false;
  }

  private boolean isInvalidClickData(CPS cps) {
    return cps.isEmpty() || cps.getClicks().size() < getConfiguration().getMinClicks();
  }

  /**
   * Calculate the total delay (in milliseconds) between all player clicks.
   *
   * @param clicks the list of clicks
   * @return the total delay between clicks
   */
  private double calculateTotalDelay(List<Click> clicks) {
    if (clicks.size() < 2) return Double.MAX_VALUE; // Not enough clicks to calculate delay

    long totalDelay = 0;
    for (int i = 1; i < clicks.size(); i++) {
      totalDelay += clicks.get(i).getTime() - clicks.get(i - 1).getTime();
    }

    return (double) totalDelay;
  }

  /**
   * Scale the total expected delay based on the player's CPS. If the player's CPS is lower than the
   * total 100 clicks range, this will scale down the delay accordingly.
   *
   * @param cps the player's clicks per second
   * @return the expected scaled delay for the given CPS
   */
  private double scaleCPSDelay(int cps) {
    double scalingFactor = (double) cps / 100;
    return scalingFactor * getConfiguration().getMaxExpectedClickDelayMs();
  }
}
