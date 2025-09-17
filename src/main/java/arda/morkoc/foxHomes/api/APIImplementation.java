package arda.morkoc.foxHomes.api;

import arda.morkoc.foxHomes.FoxHomes;
import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class APIImplementation implements FoxHomesAPI {

    private final FoxHomes plugin;

    public APIImplementation(FoxHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Home> getHome(UUID playerUUID, String homeName) {
        return plugin.getDatabaseManager().getActiveData().getHome(playerUUID, homeName);
    }

    @Override
    public CompletableFuture<List<Home>> getHomes(UUID playerUUID) {
        return plugin.getDatabaseManager().getActiveData().getHomes(playerUUID);
    }

    @Override
    public CompletableFuture<Void> setHome(UUID playerUUID, String homeName, Location location) {
        return plugin.getDatabaseManager().getActiveData().setHome(playerUUID, homeName, location);
    }

    @Override
    public CompletableFuture<Void> deleteHome(UUID playerUUID, String homeName) {
        return plugin.getDatabaseManager().getActiveData().deleteHome(playerUUID, homeName);
    }

    @Override
    public CompletableFuture<List<Home>> getHomesInChunk(String world, int chunkX, int chunkZ) {
        return plugin.getDatabaseManager().getActiveData().getHomesInChunk(world, chunkX, chunkZ);
    }

    @Override
    public CompletableFuture<List<Home>> getAllHomes() {
        return plugin.getDatabaseManager().getActiveData().getAllHomes();
    }
}
