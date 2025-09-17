package arda.morkoc.foxHomes.commands;

import arda.morkoc.foxHomes.FoxHomes;
import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DelHomeCommand implements CommandExecutor, TabCompleter {

    private final FoxHomes plugin;

    public DelHomeCommand(FoxHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLangManager().sendMessage(sender, "player-only-command");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("foxhomes.delhome")) {
            plugin.getLangManager().sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            plugin.getLangManager().sendMessage(player, "usage-delhome");
            return true;
        }

        String homeName = args[0];

        plugin.getDatabaseManager().getActiveData().getHome(player.getUniqueId(), homeName).thenAccept(home -> {
            if (home == null) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        plugin.getLangManager().sendMessage(player, "home-not-found", "{home_name}", homeName));
                return;
            }

            plugin.getDatabaseManager().getActiveData().deleteHome(player.getUniqueId(), homeName)
                    .thenRun(() -> plugin.getServer().getScheduler().runTask(plugin, () ->
                            plugin.getLangManager().sendMessage(player, "home-deleted-success", "{home_name}", homeName)));
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            Player player = (Player) sender;
            return plugin.getDatabaseManager().getActiveData().getHomes(player.getUniqueId()).join().stream()
                    .map(Home::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
