package arda.morkoc.foxHomes.commands;

import arda.morkoc.foxHomes.FoxHomes;
import arda.morkoc.foxHomes.api.events.HomePreTeleportEvent;
import arda.morkoc.foxHomes.database.DatabaseManager;
import arda.morkoc.foxHomes.managers.LangManager;
import arda.morkoc.foxHomes.managers.TeleportManager;
import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private final FoxHomes plugin;
    private final LangManager lang;
    private final DatabaseManager db;
    private final TeleportManager teleportManager;

    public HomeCommand(FoxHomes plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
        this.db = plugin.getDatabaseManager();
        this.teleportManager = plugin.getTeleportManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "player-only-command");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("foxhomes.home")) {
            lang.sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            lang.sendMessage(player, "usage-home");
            return true;
        }

        String homeName = args[0];

        db.getActiveData().getHome(player.getUniqueId(), homeName).thenAccept(home -> {
            if (home == null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    lang.sendMessage(player, "home-not-found", "{home_name}", homeName);
                });
                return;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                HomePreTeleportEvent event = new HomePreTeleportEvent(player, home);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }

                teleportManager.startTeleport(player, event.getDestination(), home.getName());
            });
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
            return db.getActiveData().getHomes(player.getUniqueId()).join().stream()
                    .map(Home::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}