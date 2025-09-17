package arda.morkoc.foxHomes.managers;

import arda.morkoc.foxHomes.FoxHomes;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public class TeleportManager {

    private final FoxHomes plugin;
    private final Map<UUID, BukkitTask> teleportTasks = new HashMap<>();

    public TeleportManager(FoxHomes plugin) {
        this.plugin = plugin;
    }

    public void startTeleport(Player player, Location location, String homeName) {
        if (teleportTasks.containsKey(player.getUniqueId())) {
            return;
        }

        int delay = getPlayerDelay(player);

        if (delay <= 0 || player.hasPermission("foxhomes.bypass.cooldown")) {
            player.teleport(location);
            plugin.getLangManager().sendMessage(player, "home-teleport-success", "{home_name}", homeName);
            return;
        }

        plugin.getLangManager().sendMessage(player, "home-teleport-start",
                "{home_name}", homeName,
                "{time}", String.valueOf(delay));

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(location);
                plugin.getLangManager().sendMessage(player, "home-teleport-success", "{home_name}", homeName);
                teleportTasks.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, delay * 20L);

        teleportTasks.put(player.getUniqueId(), task);
    }

    public void cancelTeleport(Player player) {
        if (teleportTasks.containsKey(player.getUniqueId())) {
            teleportTasks.get(player.getUniqueId()).cancel();
            teleportTasks.remove(player.getUniqueId());
            plugin.getLangManager().sendMessage(player, "home-teleport-cancelled");
        }
    }

    public boolean isTeleporting(Player player) {
        return teleportTasks.containsKey(player.getUniqueId());
    }

    private int getPlayerDelay(Player player) {
        return player.getEffectivePermissions().stream()
                .filter(p -> p.getPermission().startsWith("foxhomes.cooldown."))
                .flatMapToInt(p -> {
                    try {
                        return IntStream.of(Integer.parseInt(p.getPermission().substring(p.getPermission().lastIndexOf('.') + 1)));
                    } catch (NumberFormatException e) {
                        return IntStream.empty();
                    }
                })
                .min()
                .orElse(plugin.getConfigManager().getDefaultDelay());
    }
}