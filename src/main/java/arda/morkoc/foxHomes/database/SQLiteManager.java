package arda.morkoc.foxHomes.database;

import arda.morkoc.foxHomes.FoxHomes;
import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SQLiteManager implements IData {

    private final FoxHomes plugin;
    private Connection connection;

    public SQLiteManager(FoxHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        File dbFile = new File(plugin.getDataFolder(), "homes.db");
        if (!dbFile.exists()) {
            try {
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create SQLite database file!", e);
                return;
            }
        }

        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + dbFile.getPath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("SQLite connection established.");
            createTables();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to SQLite database!", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error closing SQLite connection!", e);
        }
    }

    @Override
    public void createTables() {
        String sqlHomes = "CREATE TABLE IF NOT EXISTS homes (" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "home_name VARCHAR(32) NOT NULL," +
                "world VARCHAR(64) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw FLOAT NOT NULL," +
                "pitch FLOAT NOT NULL," +
                "chunk_x INT NOT NULL," +
                "chunk_z INT NOT NULL," +
                "PRIMARY KEY (player_uuid, home_name)" +
                ");";

        String sqlIndex = "CREATE INDEX IF NOT EXISTS idx_chunk ON homes (world, chunk_x, chunk_z);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlHomes);
            stmt.execute(sqlIndex);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create SQLite tables!", e);
        }
    }

    @Override
    public CompletableFuture<Void> setHome(UUID uuid, String homeName, Location location) {
        return CompletableFuture.runAsync(() -> {
            String sql = "REPLACE INTO homes (player_uuid, home_name, world, x, y, z, yaw, pitch, chunk_x, chunk_z) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, homeName.toLowerCase());
                pstmt.setString(3, location.getWorld().getName());
                pstmt.setDouble(4, location.getX());
                pstmt.setDouble(5, location.getY());
                pstmt.setDouble(6, location.getZ());
                pstmt.setFloat(7, location.getYaw());
                pstmt.setFloat(8, location.getPitch());
                pstmt.setInt(9, location.getChunk().getX());
                pstmt.setInt(10, location.getChunk().getZ());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not set home in SQLite!", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<Home>> getHomesInChunk(String world, int chunkX, int chunkZ) {
        return CompletableFuture.supplyAsync(() -> {
            List<Home> homes = new ArrayList<>();
            String sql = "SELECT * FROM homes WHERE world = ? AND chunk_x = ? AND chunk_z = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, world);
                pstmt.setInt(2, chunkX);
                pstmt.setInt(3, chunkZ);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    homes.add(new Home(UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("home_name"), rs.getString("world"),
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch")));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not get homes in chunk from SQLite!", e);
            }
            return homes;
        });
    }

    @Override
    public CompletableFuture<List<Home>> getAllHomes() {
        return CompletableFuture.supplyAsync(() -> {
            List<Home> homes = new ArrayList<>();
            String sql = "SELECT * FROM homes;";
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    homes.add(new Home(UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("home_name"), rs.getString("world"),
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch")));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not get all homes from SQLite!", e);
            }
            return homes;
        });
    }

    @Override
    public CompletableFuture<Void> deleteHome(UUID uuid, String homeName) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM homes WHERE player_uuid = ? AND home_name = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, homeName.toLowerCase());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not delete home from SQLite!", e);
            }
        });
    }

    @Override
    public CompletableFuture<Home> getHome(UUID uuid, String homeName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM homes WHERE player_uuid = ? AND home_name = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, homeName.toLowerCase());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Home(uuid, rs.getString("home_name"), rs.getString("world"),
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch"));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not get home from SQLite!", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<Home>> getHomes(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Home> homes = new ArrayList<>();
            String sql = "SELECT * FROM homes WHERE player_uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    homes.add(new Home(uuid, rs.getString("home_name"), rs.getString("world"),
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch")));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not get homes from SQLite!", e);
            }
            return homes;
        });
    }

    @Override
    public CompletableFuture<Integer> getHomeCount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM homes WHERE player_uuid = ?;";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not get home count from SQLite!", e);
            }
            return 0;
        });
    }
}
