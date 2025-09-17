package arda.morkoc.foxHomes.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class Home {
    private final UUID owner;
    private final String name;
    private final Location location;

    public Home(UUID owner, String name, Location location) {
        this.owner = owner;
        this.name = name;
        this.location = location;
    }

    public Home(UUID owner, String name, String world, double x, double y, double z, float yaw, float pitch) {
        this.owner = owner;
        this.name = name;
        this.location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
}
