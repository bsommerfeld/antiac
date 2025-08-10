package de.bsommerfeld.antiac.messages;

import de.bsommerfeld.antiac.AntiAC;
import de.bsommerfeld.antiac.logging.LogManager;
import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages messages for the AntiAC plugin. Handles loading, saving, and retrieving messages from a properties file.
 */
public class Messages {

    private static final Properties PROPERTIES = new Properties();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final String PLACEHOLDER_PATTERN = "%([a-zA-Z0-9_]+)%";

    private static String propertiesFile;

    /**
     * Sets up the messages system by initializing the properties file.
     */
    public static void setup() {
        propertiesFile = AntiAC.getInstance().getDataFolder() + File.separator + "messages.properties";
        createPropertiesFileIfNotExists();
        loadPropertiesFromFile();
        saveProperties();
    }

    /**
     * Creates the properties file if it doesn't exist and loads default messages.
     */
    private static void createPropertiesFileIfNotExists() {
        File file = new File(propertiesFile);
        if (!file.exists()) {
            try {
                ensureFile(file);
                Properties defaultProperties = loadMessagePropertiesFromResources();
                if (defaultProperties != null) {
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        defaultProperties.store(out, "AntiAC Messages");
                        PROPERTIES.putAll(defaultProperties);
                    }
                }
            } catch (IOException e) {
                logError("Failed to create properties file", e);
            }
        }
    }

    /**
     * Loads default messages from the resources.
     *
     * @return Properties object containing default messages
     */
    private static Properties loadMessagePropertiesFromResources() {
        Properties tempProperties = new Properties();
        try (InputStream defaultPropertiesStream =
                     Messages.class.getResourceAsStream("/messages.properties")) {
            if (defaultPropertiesStream == null) {
                logError("Default messages.properties not found in resources", null);
                return null;
            }
            tempProperties.load(defaultPropertiesStream);
        } catch (IOException e) {
            logError("Failed to load default messages from resources", e);
        }
        return tempProperties;
    }

    /**
     * Ensures that the file and its parent directories exist.
     *
     * @param file The file to ensure
     *
     * @throws IOException If an I/O error occurs
     */
    private static void ensureFile(File file) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
    }

    /**
     * Loads properties from the file.
     */
    private static void loadPropertiesFromFile() {
        File file = new File(propertiesFile);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(propertiesFile)) {
                PROPERTIES.load(in);
            } catch (IOException e) {
                logError("Failed to load properties from file", e);
            }
        } else {
            logError("Properties file does not exist: " + propertiesFile, null);
        }
    }

    /**
     * Saves properties to the file.
     */
    private static void saveProperties() {
        try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
            PROPERTIES.store(out, "AntiAC Messages");
        } catch (IOException e) {
            logError("Failed to save properties", e);
        }
    }

    /**
     * Gets a message from the properties file.
     *
     * @param key The key of the message
     *
     * @return The message, or the key if the message is not found
     */
    public static String getString(String key) {
        return PROPERTIES.getProperty(key, key);
    }

    /**
     * Gets a formatted message with color codes processed.
     *
     * @param key The key of the message
     *
     * @return The formatted message with colors, or the key if the message is not found
     */
    public static String getColoredString(String key) {
        String message = getString(key);
        return formatColors(message);
    }

    /**
     * Gets a formatted message with placeholders replaced.
     *
     * @param key          The key of the message
     * @param placeholders The placeholders to replace in the format: placeholder1, value1, placeholder2, value2, ...
     *
     * @return The formatted message with placeholders replaced
     */
    public static String getFormattedString(String key, Object... placeholders) {
        String message = getString(key);

        if (placeholders.length % 2 != 0) {
            logError("Invalid number of placeholder arguments for key: " + key, null);
            return message;
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = "%" + placeholders[i] + "%";
            String value = String.valueOf(placeholders[i + 1]);
            message = message.replace(placeholder, value);
        }

        return formatColors(message);
    }

    /**
     * Sets a message in the properties file.
     *
     * @param key   The key of the message
     * @param value The value of the message
     */
    public static void setString(String key, String value) {
        PROPERTIES.setProperty(key, value);
        saveProperties();
    }

    /**
     * Formats color codes in a message.
     *
     * @param message The message to format
     *
     * @return The formatted message
     */
    private static String formatColors(String message) {
        if (message == null) {
            return "";
        }

        // Process hex colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hexColor).toString());
        }
        matcher.appendTail(buffer);

        // Process standard color codes (&a, &b, etc.)
        String formattedMessage = buffer.toString();
        return ChatColor.translateAlternateColorCodes('&', formattedMessage);
    }

    /**
     * Reloads messages from the properties file.
     */
    public static void reload() {
        PROPERTIES.clear();
        loadPropertiesFromFile();
    }

    /**
     * Migrates properties from the default properties file.
     */
    public static void migrate() {
        Properties newProperties = loadMessagePropertiesFromResources();
        if (newProperties != null) {
            migrateProperties(newProperties);
            saveProperties();
        }
    }

    /**
     * Migrates properties by removing old keys and adding new ones.
     *
     * @param newProperties The new properties to migrate to
     */
    private static void migrateProperties(Properties newProperties) {
        PROPERTIES
                .keySet()
                .removeIf(key -> !newProperties.containsKey(key));
        newProperties.forEach(PROPERTIES::putIfAbsent);
    }

    /**
     * Logs an error message.
     *
     * @param message The error message
     * @param e       The exception, or null if there is no exception
     */
    private static void logError(String message, Exception e) {
        try {
            if (e != null) {
                LogManager.error(message, e);
            } else {
                LogManager.error(message);
            }
        } catch (IllegalStateException ex) {
            // LogManager not initialized yet, fall back to direct logging
            if (AntiAC.getInstance() != null) {
                if (e != null) {
                    AntiAC.getInstance().getLogger().severe(message);
                    e.printStackTrace();
                } else {
                    AntiAC.getInstance().getLogger().severe(message);
                }
            } else {
                System.err.println("[AntiAC] " + message);
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }
    }
}