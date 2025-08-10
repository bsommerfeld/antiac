package de.bsommerfeld.antiac.logging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Logger interface that uses Bukkit's logging system.
 */
@Singleton
public class BukkitLogger implements de.bsommerfeld.antiac.logging.Logger {

    private final Logger logger;
    private final boolean debugEnabled;

    /**
     * Creates a new BukkitLogger.
     *
     * @param plugin The JavaPlugin instance to get the logger from
     * @param debugEnabled Whether debug logging is enabled
     */
    @Inject
    public BukkitLogger(JavaPlugin plugin, @Named("debug") boolean debugEnabled) {
        this.logger = plugin.getLogger();
        this.debugEnabled = debugEnabled;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warning(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message) {
        logger.log(Level.SEVERE, message);
    }

    @Override
    public void error(String message, Exception e) {
        logger.log(Level.SEVERE, message, e);
    }

    @Override
    public void debug(String message) {
        if (debugEnabled) {
            logger.info("[DEBUG] " + message);
        }
    }
}