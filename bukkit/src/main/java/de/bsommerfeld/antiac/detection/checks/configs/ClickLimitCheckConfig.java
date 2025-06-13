package de.bsommerfeld.antiac.detection.checks.configs;

import de.bsommerfeld.antiac.detection.CheckConfiguration;
import de.godcipher.gutil.config.annotation.ConfigValue;
import de.godcipher.gutil.config.annotation.Configuration;
import lombok.Getter;

@Getter
@Configuration(fileName = "click-limit.yml")
public class ClickLimitCheckConfig extends CheckConfiguration {

  @ConfigValue(
      name = "click-limit",
      description = "The amount of clicks a player is allowed to perform within a second")
  private int clickLimit = 40;
}
