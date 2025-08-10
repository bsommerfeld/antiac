package de.bsommerfeld.antiac.logging;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.bsommerfeld.antiac.AntiAC;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Guice module for configuring logging services.
 */
public class LoggingModule extends AbstractModule {

    private final AntiAC plugin;
    private final boolean debugEnabled;

    /**
     * Creates a new LoggingModule.
     *
     * @param plugin The AntiAC plugin instance
     * @param debugEnabled Whether debug logging is enabled
     */
    public LoggingModule(AntiAC plugin, boolean debugEnabled) {
        this.plugin = plugin;
        this.debugEnabled = debugEnabled;
    }

    @Override
    protected void configure() {
        // Bind the Logger interface to the BukkitLogger implementation
        bind(Logger.class).to(BukkitLogger.class).in(Singleton.class);
    }

    /**
     * Provides the JavaPlugin instance for the BukkitLogger.
     *
     * @return The JavaPlugin instance
     */
    @Provides
    @Singleton
    JavaPlugin provideJavaPlugin() {
        return plugin;
    }

    /**
     * Provides the debug flag for the BukkitLogger.
     *
     * @return Whether debug logging is enabled
     */
    @Provides
    @Named("debug")
    boolean provideDebugEnabled() {
        return debugEnabled;
    }
}