package arda.morkoc.foxHomes.commands;

import arda.morkoc.foxHomes.FoxHomes;
import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class HomeListCommand implements CommandExecutor {
    private final FoxHomes plugin;

    public HomeListCommand(FoxHomes plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLangManager().sendMessage(sender, "player-only-command");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("foxhomes.homelist")) {
            plugin.getLangManager().sendMessage(player, "no-permission");
            return true;
        }

        plugin.getDatabaseManager().getActiveData().getHomes(player.getUniqueId()).thenAccept(homes -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (homes == null || homes.isEmpty()) {
                    plugin.getLangManager().sendMessage(player, "home-list-empty");
                    return;
                }
                int maxHomes = getMaxHomes(player);
                String max = player.hasPermission("foxhomes.unlimited") ? "∞" : String.valueOf(maxHomes);

                plugin.getLangManager().sendMessage(player, "home-list-header",
                        "{count}", String.valueOf(homes.size()),
                        "{max}", max);

                String homeListString = homes.stream()
                        .map(Home::getName)
                        .collect(Collectors.joining(", "));

                String rawListItem = plugin.getLangManager().getRawMessage("home-list-item");
                String replacedMessage = rawListItem.replace("{home_name}", homeListString);
                String translatedMessage = plugin.getLangManager().translateColors(replacedMessage);
                sender.sendMessage(translatedMessage);
            });
        });
        return true;
    }

    private int getMaxHomes(Player player) {
        if (player.hasPermission("foxhomes.unlimited")) return Integer.MAX_VALUE;
        return player.getEffectivePermissions().stream()
                .filter(p -> p.getPermission().startsWith("foxhomes.maxhomes."))
                .mapToInt(p -> { try { return Integer.parseInt(p.getPermission().substring(p.getPermission().lastIndexOf('.') + 1)); } catch (NumberFormatException e) { return 0; }})
                .max().orElse(plugin.getConfigManager().getDefaultMaxHomes());
    }
}
