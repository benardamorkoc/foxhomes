package arda.morkoc.foxHomes.api.events;

import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HomePreTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Home home;
    private Location destination;
    private boolean isCancelled;

    public HomePreTeleportEvent(Player player, Home home) {
        this.player = player;
        this.home = home;
        this.destination = home.getLocation();
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Home getHome() {
        return home;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
