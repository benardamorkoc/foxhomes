package arda.morkoc.foxHomes.database;

import arda.morkoc.foxHomes.FoxHomes;
import arda.morkoc.foxHomes.objects.Home;
import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MySQLManager implements IData {

    private final FoxHomes plugin;
    private Connection connection;

    public MySQLManager(FoxHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        String host = plugin.getConfigManager().getDbHost();
        int port = plugin.getConfigManager().getDbPort();
        String dbName = plugin.getConfigManager().getDbName();
        String user = plugin.getConfigManager().getDbUsername();
        String pass = plugin.getConfigManager().getDbPassword();

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?autoReconnect=true&useSSL=false";

        try {
            connection = DriverManager.getConnection(url, user, pass);
            plugin.getLogger().info("MySQL connection established.");
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to MySQL database!", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error closing MySQL connection!", e);
        }
    }

    @Override
    public void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS homes (" +
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
                "PRIMARY KEY (player_uuid, home_name)," +
                "INDEX idx_chunk (world, chunk_x, chunk_z)" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create MySQL tables!", e);
        }
    }

    @Override
    public CompletableFuture<Void> setHome(UUID uuid, String homeName, Location location) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO homes (player_uuid, home_name, world, x, y, z, yaw, pitch, chunk_x, chunk_z) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, chunk_x = ?, chunk_z = ?;";
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
                pstmt.setString(11, location.getWorld().getName());
                pstmt.setDouble(12, location.getX());
                pstmt.setDouble(13, location.getY());
                pstmt.setDouble(14, location.getZ());
                pstmt.setFloat(15, location.getYaw());
                pstmt.setFloat(16, location.getPitch());
                pstmt.setInt(17, location.getChunk().getX());
                pstmt.setInt(18, location.getChunk().getZ());

                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not set home in MySQL!", e);
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
                plugin.getLogger().log(Level.SEVERE, "Could not get homes in chunk from MySQL!", e);
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
                plugin.getLogger().log(Level.SEVERE, "Could not get all homes from MySQL!", e);
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
                plugin.getLogger().log(Level.SEVERE, "Could not delete home from MySQL!", e);
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
                plugin.getLogger().log(Level.SEVERE, "Could not get home from MySQL!", e);
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
                plugin.getLogger().log(Level.SEVERE, "Could not get homes from MySQL!", e);
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
                plugin.getLogger().log(Level.SEVERE, "Could not get home count from MySQL!", e);
            }
            return 0;
        });
    }
}