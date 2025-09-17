package arda.morkoc.foxHomes.api;

import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FoxHomesAPI {

    CompletableFuture<Home> getHome(UUID playerUUID, String homeName);

    default CompletableFuture<Home> getHome(Player player, String homeName) {
        return getHome(player.getUniqueId(), homeName);
    }

    CompletableFuture<List<Home>> getHomes(UUID playerUUID);

    default CompletableFuture<List<Home>> getHomes(Player player) {
        return getHomes(player.getUniqueId());
    }

    CompletableFuture<Void> setHome(UUID playerUUID, String homeName, Location location);

    default CompletableFuture<Void> setHome(Player player, String homeName, Location location) {
        return setHome(player.getUniqueId(), homeName, location);
    }

    CompletableFuture<Void> deleteHome(UUID playerUUID, String homeName);

    default CompletableFuture<Void> deleteHome(Player player, String homeName) {
        return deleteHome(player.getUniqueId(), homeName);
    }

    CompletableFuture<List<Home>> getHomesInChunk(String world, int chunkX, int chunkZ);

    default CompletableFuture<List<Home>> getHomesInChunk(World world, int chunkX, int chunkZ) {
        return getHomesInChunk(world.getName(), chunkX, chunkZ);
    }

    default CompletableFuture<List<Home>> getHomesInChunk(Chunk chunk) {
        return getHomesInChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    CompletableFuture<List<Home>> getAllHomes();
}