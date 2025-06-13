package de.bsommerfeld.antiac.messages;

import de.bsommerfeld.antiac.AntiAC;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Messages {

  private static final Properties PROPERTIES = new Properties();
  private static String propertiesFile;

  public static void setup() {
    propertiesFile = AntiAC.getInstance().getDataFolder() + File.separator + "messages.properties";
    createPropertiesFileIfNotExists();
    loadPropertiesFromFile();
    saveProperties();
  }

  private static void createPropertiesFileIfNotExists() {
    File file = new File(propertiesFile);
    if (!file.exists()) {
      try {
        ensureFile(file);
        Properties defaultProperties = loadMessagePropertiesFromResources();
        if (defaultProperties != null) {
          try (FileOutputStream out = new FileOutputStream(file)) {
            defaultProperties.store(out, null);
            PROPERTIES.putAll(defaultProperties);
          }
        }
      } catch (IOException e) {
      }
    }
  }

  private static Properties loadMessagePropertiesFromResources() {
    Properties tempProperties = new Properties();
    try (InputStream defaultPropertiesStream =
                 Messages.class.getResourceAsStream("/messages.properties")) {
      if (defaultPropertiesStream == null) {
        return null;
      }
      tempProperties.load(defaultPropertiesStream);
    } catch (IOException e) {
    }
    return tempProperties;
  }

  private static void ensureFile(File file) throws IOException {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    if (file.createNewFile()) {
    }
  }

  private static void loadPropertiesFromFile() {
    File file = new File(propertiesFile);
    if (file.exists()) {
      try (FileInputStream in = new FileInputStream(propertiesFile)) {
        PROPERTIES.load(in);
      } catch (IOException e) {
      }
    } else {
    }
  }

  private static void saveProperties() {
    try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
      PROPERTIES.store(out, null);
    } catch (IOException e) {
    }
  }

  public static String getString(String key) {
    return PROPERTIES.getProperty(key);
  }

  public static void migrate() {
    Properties newProperties = loadMessagePropertiesFromResources();
    if (newProperties != null) {
      migrateProperties(newProperties);
      saveProperties();
    }
  }

  private static void migrateProperties(Properties newProperties) {
    PROPERTIES
            .keySet()
            .removeIf(key -> !newProperties.containsKey(key));
    newProperties.forEach(PROPERTIES::putIfAbsent);
  }
}