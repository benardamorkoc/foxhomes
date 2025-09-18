package arda.morkoc.foxHomes.commands;

import arda.morkoc.foxHomes.FoxHomes;
import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FoxHomesAdminCommand implements CommandExecutor, TabCompleter {

    private final FoxHomes plugin;

    public FoxHomesAdminCommand(FoxHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("foxhomes.admin")) {
            plugin.getLangManager().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getLangManager().translateColors("&e/foxhomes <reload|list|delhome|sethome|movehome>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "delhome":
                handleDelHome(sender, args);
                break;
            case "list":
                handleList(sender, args);
                break;
            case "sethome":
                handleSetHome(sender, args);
                break;
            case "movehome":
                handleMoveHome(sender, args);
                break;
            default:
                sender.sendMessage(plugin.getLangManager().translateColors("&cUnknown subcommand."));
                break;
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().loadConfig();
        plugin.getLangManager().loadLangFile();
        plugin.getDatabaseManager().disconnect();
        plugin.getDatabaseManager().connect();
        plugin.getLangManager().sendMessage(sender, "reload-success");
    }

    private void handleDelHome(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getLangManager().sendMessage(sender, "usage-foxhomes-delhome");
            return;
        }
        String playerName = args[1];
        String homeName = args[2];

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            plugin.getLangManager().sendMessage(sender, "admin-player-not-found", "{player_name}", playerName);
            return;
        }

        UUID targetUUID = target.getUniqueId();

        plugin.getDatabaseManager().getActiveData().getHome(targetUUID, homeName).thenAccept(home -> {
            if (home == null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getLangManager().sendMessage(sender, "admin-home-not-found", "{player_name}", target.getName(), "{home_name}", homeName);
                });
                return;
            }

            plugin.getDatabaseManager().getActiveData().deleteHome(targetUUID, homeName).thenRun(() -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getLangManager().sendMessage(sender, "admin-home-deleted", "{player_name}", target.getName(), "{home_name}", homeName);
                });
            });
        });
    }

    private void handleList(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getLangManager().sendMessage(sender, "usage-foxhomes-list");
            return;
        }
        String playerName = args[1];

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            plugin.getLangManager().sendMessage(sender, "admin-player-not-found", "{player_name}", playerName);
            return;
        }

        plugin.getDatabaseManager().getActiveData().getHomes(target.getUniqueId()).thenAccept(homes -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (homes == null || homes.isEmpty()) {
                    plugin.getLangManager().sendMessage(sender, "admin-home-list-empty", "{player_name}", target.getName());
                    return;
                }
                plugin.getLangManager().sendMessage(sender, "admin-home-list-header", "{player_name}", target.getName());
                String homeListString = homes.stream().map(Home::getName).collect(Collectors.joining(", "));
                sender.sendMessage(plugin.getLangManager().translateColors("&e" + homeListString));
            });
        });
    }

    private void handleSetHome(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLangManager().sendMessage(sender, "admin-must-be-player");
            return;
        }
        Player admin = (Player) sender;

        if (args.length < 3) {
            plugin.getLangManager().sendMessage(sender, "usage-foxhomes-sethome");
            return;
        }
        String playerName = args[1];
        String homeName = args[2];

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            plugin.getLangManager().sendMessage(sender, "admin-player-not-found", "{player_name}", playerName);
            return;
        }

        plugin.getDatabaseManager().getActiveData().setHome(target.getUniqueId(), homeName, admin.getLocation())
                .thenRun(() -> {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getLangManager().sendMessage(sender, "admin-home-set-success",
                                "{player_name}", target.getName(),
                                "{home_name}", homeName);
                    });
                });
    }

    private void handleMoveHome(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLangManager().sendMessage(sender, "admin-must-be-player");
            return;
        }
        Player admin = (Player) sender;

        if (args.length < 3) {
            plugin.getLangManager().sendMessage(sender, "usage-foxhomes-movehome");
            return;
        }
        String playerName = args[1];
        String homeName = args[2];

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            plugin.getLangManager().sendMessage(sender, "admin-player-not-found", "{player_name}", playerName);
            return;
        }

        plugin.getDatabaseManager().getActiveData().getHome(target.getUniqueId(), homeName).thenAccept(home -> {
            if (home == null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getLangManager().sendMessage(sender, "admin-home-does-not-exist-move",
                            "{player_name}", target.getName(),
                            "{home_name}", homeName);
                });
                return;
            }

            plugin.getDatabaseManager().getActiveData().setHome(target.getUniqueId(), homeName, admin.getLocation())
                    .thenRun(() -> {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            plugin.getLangManager().sendMessage(sender, "admin-home-moved-success",
                                    "{player_name}", target.getName(),
                                    "{home_name}", homeName);
                        });
                    });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "delhome", "list", "sethome", "movehome").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("delhome") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("sethome") || args[0].equalsIgnoreCase("movehome"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("delhome") || args[0].equalsIgnoreCase("movehome"))) {
            String playerName = args[1];
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

            if (target == null || !target.hasPlayedBefore()) {
                return Collections.emptyList();
            }

            return plugin.getDatabaseManager().getActiveData().getHomes(target.getUniqueId()).join().stream()
                    .map(Home::getName)
                    .filter(homeName -> homeName.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}