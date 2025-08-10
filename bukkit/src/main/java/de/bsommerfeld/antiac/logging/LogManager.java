package de.bsommerfeld.antiac.logging;

import com.google.inject.Injector;

/**
 * Static helper class for accessing the logger from non-injectable contexts.
 */
public class LogManager {

    private static Logger logger;

    private LogManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initializes the LogManager with an injector.
     * This should be called once during plugin startup.
     *
     * @param injector The Guice injector
     */
    public static void initialize(Injector injector) {
        logger = injector.getInstance(Logger.class);
    }

    /**
     * Gets the logger instance.
     *
     * @return The logger instance
     * @throws IllegalStateException if the LogManager has not been initialized
     */
    public static Logger getLogger() {
        if (logger == null) {
            throw new IllegalStateException("LogManager has not been initialized");
        }
        return logger;
    }

    /**
     * Logs an informational message.
     *
     * @param message The message to log
     */
    public static void info(String message) {
        getLogger().info(message);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log
     */
    public static void warning(String message) {
        getLogger().warning(message);
    }

    /**
     * Logs an error message.
     *
     * @param message The message to log
     */
    public static void error(String message) {
        getLogger().error(message);
    }

    /**
     * Logs an error message with an associated exception.
     *
     * @param message The message to log
     * @param e The exception associated with the error
     */
    public static void error(String message, Exception e) {
        getLogger().error(message, e);
    }

    /**
     * Logs a debug message.
     * These messages are only logged if debug mode is enabled.
     *
     * @param message The message to log
     */
    public static void debug(String message) {
        getLogger().debug(message);
    }
}