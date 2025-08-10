package de.bsommerfeld.antiac.logging;

/**
 * Interface for logging operations in AntiAC.
 * Provides methods for logging messages at different severity levels.
 */
public interface Logger {

    /**
     * Logs an informational message.
     *
     * @param message The message to log
     */
    void info(String message);

    /**
     * Logs a warning message.
     *
     * @param message The message to log
     */
    void warning(String message);

    /**
     * Logs an error message.
     *
     * @param message The message to log
     */
    void error(String message);

    /**
     * Logs an error message with an associated exception.
     *
     * @param message The message to log
     * @param e The exception associated with the error
     */
    void error(String message, Exception e);

    /**
     * Logs a debug message.
     * These messages are only logged if debug mode is enabled.
     *
     * @param message The message to log
     */
    void debug(String message);
}