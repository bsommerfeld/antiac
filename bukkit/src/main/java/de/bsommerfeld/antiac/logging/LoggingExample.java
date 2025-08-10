package de.bsommerfeld.antiac.logging;

import com.google.inject.Inject;
import de.bsommerfeld.antiac.AntiAC;

/**
 * Example class demonstrating how to use the new logging module.
 * This class shows both dependency injection and static access patterns.
 */
public class LoggingExample {

    private final Logger logger;

    /**
     * Constructor with injected logger.
     * This is the preferred way to get a logger in classes that can use dependency injection.
     *
     * @param logger The injected logger instance
     */
    @Inject
    public LoggingExample(Logger logger) {
        this.logger = logger;
        
        // Example usage of the injected logger
        this.logger.info("LoggingExample initialized with injected logger");
    }

    /**
     * Example method using the injected logger.
     */
    public void logWithInjectedLogger() {
        logger.info("This is an info message from the injected logger");
        logger.warning("This is a warning message from the injected logger");
        logger.error("This is an error message from the injected logger");
        logger.debug("This is a debug message from the injected logger (only shown if debug is enabled)");
        
        try {
            // Simulate an exception
            throw new RuntimeException("Test exception");
        } catch (Exception e) {
            logger.error("Caught an exception", e);
        }
    }

    /**
     * Example method using the static LogManager.
     * This approach is useful for static methods or classes that can't use dependency injection.
     */
    public static void logWithStaticManager() {
        LogManager.info("This is an info message from the static LogManager");
        LogManager.warning("This is a warning message from the static LogManager");
        LogManager.error("This is an error message from the static LogManager");
        LogManager.debug("This is a debug message from the static LogManager (only shown if debug is enabled)");
        
        try {
            // Simulate an exception
            throw new RuntimeException("Test exception");
        } catch (Exception e) {
            LogManager.error("Caught an exception", e);
        }
    }

    /**
     * Example of how to get an instance of this class from the Guice injector.
     */
    public static void demonstrateUsage() {
        // Get an instance using Guice
        LoggingExample example = AntiAC.getInjector().getInstance(LoggingExample.class);
        
        // Use the injected logger
        example.logWithInjectedLogger();
        
        // Use the static LogManager
        logWithStaticManager();
    }
}