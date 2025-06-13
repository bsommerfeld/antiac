package de.bsommerfeld.antiac.click.impl;

import de.bsommerfeld.antiac.click.Click;
import de.bsommerfeld.antiac.click.ClickType;
import java.util.Objects;

public class DefaultClick implements Click {

  private final long timestamp;
  private final ClickType clickType;

  public DefaultClick(ClickType clickType) {
    this.timestamp = System.currentTimeMillis();
    this.clickType = Objects.requireNonNull(clickType);
  }

  @Override
  public long timestamp() {
    return timestamp;
  }

  @Override
  public ClickType clickType() {
    return clickType;
  }
}
