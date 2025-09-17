package arda.morkoc.foxHomes.listeners;

import arda.morkoc.foxHomes.FoxHomes;
import arda.morkoc.foxHomes.managers.TeleportManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final FoxHomes plugin;
    private final TeleportManager teleportManager;

    public PlayerMoveListener(FoxHomes plugin) {
        this.plugin = plugin;
        this.teleportManager = plugin.getTeleportManager();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!teleportManager.isTeleporting(player)) {
            return;
        }

        if (!plugin.getConfigManager().isCancelOnMove() || player.hasPermission("foxhomes.bypass.move")) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            teleportManager.cancelTeleport(player);
        }
    }
}