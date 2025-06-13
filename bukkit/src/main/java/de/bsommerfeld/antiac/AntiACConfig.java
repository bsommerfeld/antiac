package de.bsommerfeld.antiac;

import de.bsommerfeld.jshepherd.annotation.Key;
import de.bsommerfeld.jshepherd.core.ConfigurablePojo;

public class AntiACConfig extends ConfigurablePojo<AntiACConfig> {

  @Key("database-url")
  private String databaseUrl = "";

  @Key("database-username")
  private String databaseUsername = "";

  @Key("database-password")
  private String databasePassword = "";
}
