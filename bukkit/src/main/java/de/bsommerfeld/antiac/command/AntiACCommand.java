package de.bsommerfeld.antiac.command;

import com.google.inject.Inject;
import de.bsommerfeld.antiac.AntiACConfig;
import de.bsommerfeld.antiac.capture.ClickCollector;
import de.bsommerfeld.antiac.logging.LogManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AntiACCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ClickCollector collector;
    private final AntiACConfig config;

    @Inject
    public AntiACCommand(JavaPlugin plugin, ClickCollector collector, AntiACConfig config) {
        this.plugin = plugin;
        this.collector = collector;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /" + label + " check <player>");
            return true;
        }
        String sub = args[0].toLowerCase();
        if (!sub.equals("check")) {
            sender.sendMessage("Unknown subcommand. Usage: /" + label + " check <player>");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Please specify a player: /" + label + " check <player>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found or not online: " + args[1]);
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player viewer = (Player) sender;
        startMonitor(viewer, target.getUniqueId(), target.getName());
        sender.sendMessage("Monitoring clicks for " + target.getName() + " in action bar for " + config.getMonitorDurationSeconds() + "s.");
        return true;
    }

    private void startMonitor(Player viewer, UUID targetId, String targetName) {
        final long durationMs = config.getMonitorDurationSeconds() * 1000L;
        new BukkitRunnable() {
            final long startedAt = System.currentTimeMillis();
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (!viewer.isOnline()) {
                    cancel();
                    return;
                }
                List<Long> window = collector.getWindowTimestamps(targetId, now);
                int liveWindow = window.size();
                long oneSecAgo = now - 1000L;
                int lastSecond = 0;
                for (int i = window.size() - 1; i >= 0; i--) {
                    if (window.get(i) >= oneSecAgo) lastSecond++;
                    else break;
                }
                double cps = lastSecond;
                String msg = String.format("§bAntiAC§7 | §fTarget: §a%s§7 | §fCPS: §a%.0f§7 | §fLive: §a%d", targetName, cps, liveWindow);
                viewer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                if (now - startedAt >= durationMs) {
                    viewer.sendMessage("Stopped monitoring " + targetName + ".");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("check");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("check")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
