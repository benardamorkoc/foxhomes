package arda.morkoc.foxHomes.managers;

import arda.morkoc.foxHomes.FoxHomes;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final FoxHomes plugin;
    private FileConfiguration config;

    public ConfigManager(FoxHomes plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    public String getDbHost() { return config.getString("database.host"); }
    public int getDbPort() { return config.getInt("database.port"); }
    public String getDbName() { return config.getString("database.database"); }
    public String getDbUsername() { return config.getString("database.username"); }
    public String getDbPassword() { return config.getString("database.password"); }

    public String getLanguage() {
        return config.getString("language", "en");
    }

    public boolean isCancelOnMove() {
        return config.getBoolean("teleport.cancel-on-move", true);
    }

    public int getDefaultDelay() {
        return config.getInt("teleport.default-delay", 5);
    }

    public int getDefaultMaxHomes() {
        return config.getInt("homes.default-max-homes", 3);
    }

    public List<String> getAllowedWorlds() {
        return config.getStringList("homes.allowed-worlds");
    }

    public String getAllowedNameRegex() {
        return config.getString("homes.allowed-name-regex", "^[a-zA-Z0-9]+$");
    }

    public boolean isCommandOverrideEnabled() {
        return config.getBoolean("command-override.enabled", true);
    }

    public List<String> getHomeAliases() {
        return config.getStringList("command-override.aliases.home");
    }

    public List<String> getSetHomeAliases() {
        return config.getStringList("command-override.aliases.sethome");
    }

    public List<String> getDelHomeAliases() {
        return config.getStringList("command-override.aliases.delhome");
    }

    public List<String> getHomeListAliases() {
        return config.getStringList("command-override.aliases.homelist");
    }
}