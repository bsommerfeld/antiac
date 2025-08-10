package de.bsommerfeld.antiac.export;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.antiac.detection.features.FeatureVector;
import de.bsommerfeld.antiac.logging.LogManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Singleton
public final class CsvExportService {
    private final JavaPlugin plugin;
    private final File file;
    private volatile boolean headerWritten = false;

    @Inject
    public CsvExportService(JavaPlugin plugin) {
        this.plugin = plugin;
        File dir = plugin.getDataFolder();
        if (!dir.exists()) dir.mkdirs();
        File exportDir = new File(dir, "export");
        if (!exportDir.exists()) exportDir.mkdirs();
        this.file = new File(exportDir, "features.csv");
    }

    public synchronized void append(FeatureVector fv) {
        try {
            boolean writeHeaderNow = !file.exists();
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8)) {
                if (writeHeaderNow && !headerWritten) {
                    writer.write(fv.toCsvHeader());
                    writer.write('\n');
                    headerWritten = true;
                }
                writer.write(fv.toCsvRow());
                writer.write('\n');
            }
        } catch (IOException e) {
            LogManager.error("Failed to append feature row to CSV", e);
        }
    }
}
