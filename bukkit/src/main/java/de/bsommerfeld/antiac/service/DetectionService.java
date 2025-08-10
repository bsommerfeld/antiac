package de.bsommerfeld.antiac.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.antiac.capture.ClickCollector;
import de.bsommerfeld.antiac.detection.CheckManager;
import de.bsommerfeld.antiac.detection.CheckResult;
import de.bsommerfeld.antiac.detection.features.FeatureExtractor;
import de.bsommerfeld.antiac.detection.features.FeatureVector;
import de.bsommerfeld.antiac.export.CsvExportService;
import de.bsommerfeld.antiac.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public final class DetectionService {
    private final JavaPlugin plugin;
    private final ClickCollector collector;
    private final FeatureExtractor extractor;
    private final CsvExportService exporter;
    private final CheckManager checkManager;

    private BukkitTask task;

    @Inject
    public DetectionService(JavaPlugin plugin,
                            ClickCollector collector,
                            FeatureExtractor extractor,
                            CsvExportService exporter,
                            CheckManager checkManager) {
        this.plugin = plugin;
        this.collector = collector;
        this.extractor = extractor;
        this.exporter = exporter;
        this.checkManager = checkManager;
    }

    public void start(long periodTicks) {
        if (task != null) return;
        // Schedule a lightweight synchronous timer that offloads heavy work to an async task each tick
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::scheduleAsyncSample, periodTicks, periodTicks);
        LogManager.info("DetectionService started (async processing) with periodTicks=" + periodTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
            LogManager.info("DetectionService stopped");
        }
    }

    private void scheduleAsyncSample() {
        // Capture immutable snapshot of player UUIDs on the main thread
        List<UUID> playerIds = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            playerIds.add(p.getUniqueId());
        }
        long now = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> processPlayersAsync(playerIds, now));
    }

    private void processPlayersAsync(List<UUID> playerIds, long now) {
        long windowStart = now - collector.getWindowMillis();
        for (UUID id : playerIds) {
            List<Long> timestamps = collector.getWindowTimestamps(id, now);
            FeatureVector fv = extractor.extract(id, timestamps, windowStart, now);
            exporter.append(fv);
            for (CheckResult<?> result : checkManager.runAll(fv)) {
                if (result.successful()) {
                    LogManager.debug("Flagged: " + result);
                }
            }
        }
    }
}
