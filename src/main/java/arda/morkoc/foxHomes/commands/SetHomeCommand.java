package arda.morkoc.foxHomes.commands;

import arda.morkoc.foxHomes.FoxHomes;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.stream.IntStream;

public class SetHomeCommand implements CommandExecutor {

    private final FoxHomes plugin;

    public SetHomeCommand(FoxHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLangManager().sendMessage(sender, "player-only-command");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("foxhomes.sethome")) {
            plugin.getLangManager().sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            plugin.getLangManager().sendMessage(player, "usage-sethome");
            return true;
        }

        String homeName = args[0];
        if (!homeName.matches(plugin.getConfigManager().getAllowedNameRegex())) {
            plugin.getLangManager().sendMessage(player, "invalid-home-name");
            return true;
        }

        if (!plugin.getConfigManager().getAllowedWorlds().isEmpty() && !plugin.getConfigManager().getAllowedWorlds().contains(player.getWorld().getName())) {
            plugin.getLangManager().sendMessage(player, "world-not-allowed");
            return true;
        }

        plugin.getDatabaseManager().getActiveData().getHome(player.getUniqueId(), homeName).thenAccept(existingHome -> {
            if (existingHome != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getLangManager().sendMessage(player, "home-name-already-exists");
                });
                return;
            }

            plugin.getDatabaseManager().getActiveData().getHomeCount(player.getUniqueId()).thenAccept(count -> {
                int maxHomes = getMaxHomes(player);
                if (count >= maxHomes && !player.hasPermission("foxhomes.unlimited")) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            plugin.getLangManager().sendMessage(player, "max-homes-reached",
                                    "{max_homes}", String.valueOf(maxHomes)));
                    return;
                }

                plugin.getDatabaseManager().getActiveData().setHome(player.getUniqueId(), homeName, player.getLocation())
                        .thenRun(() -> plugin.getServer().getScheduler().runTask(plugin, () ->
                                plugin.getLangManager().sendMessage(player, "home-set-success", "{home_name}", homeName)));
            });
        });

        return true;
    }

    private int getMaxHomes(Player player) {
        if (player.hasPermission("foxhomes.unlimited")) {
            return Integer.MAX_VALUE;
        }
        return player.getEffectivePermissions().stream()
                .filter(p -> p.getPermission().startsWith("foxhomes.maxhomes."))
                .mapToInt(p -> {
                    try {
                        return Integer.parseInt(p.getPermission().substring(p.getPermission().lastIndexOf('.') + 1));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(plugin.getConfigManager().getDefaultMaxHomes());
    }
}