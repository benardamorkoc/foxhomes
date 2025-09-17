package arda.morkoc.foxHomes.database;

import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IData {
    void connect();
    void disconnect();
    void createTables();

    CompletableFuture<Void> setHome(UUID uuid, String homeName, Location location);
    CompletableFuture<Void> deleteHome(UUID uuid, String homeName);
    CompletableFuture<Home> getHome(UUID uuid, String homeName);
    CompletableFuture<List<Home>> getHomes(UUID uuid);
    CompletableFuture<Integer> getHomeCount(UUID uuid);

    CompletableFuture<List<Home>> getHomesInChunk(String world, int chunkX, int chunkZ);

    CompletableFuture<List<Home>> getAllHomes();
}

