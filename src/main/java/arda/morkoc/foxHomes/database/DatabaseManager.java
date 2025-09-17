package arda.morkoc.foxHomes.database;

import arda.morkoc.foxHomes.FoxHomes;

public class DatabaseManager {

    private final FoxHomes plugin;
    private IData activeData;

    public DatabaseManager(FoxHomes plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String type = plugin.getConfigManager().getDatabaseType().toLowerCase();
        plugin.getLogger().info("Connecting to database type: " + type);

        if (type.equals("mysql")) {
            activeData = new MySQLManager(plugin);
        } else {
            activeData = new SQLiteManager(plugin);
        }

        activeData.connect();
    }

    public void disconnect() {
        if (activeData != null) {
            activeData.disconnect();
            plugin.getLogger().info("Database connection closed.");
        }
    }

    public IData getActiveData() {
        return activeData;
    }
}
