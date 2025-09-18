package arda.morkoc.foxHomes;

import arda.morkoc.foxHomes.api.APIImplementation;
import arda.morkoc.foxHomes.api.FoxHomesAPI;
import arda.morkoc.foxHomes.commands.*;
import arda.morkoc.foxHomes.database.DatabaseManager;
import arda.morkoc.foxHomes.listeners.PlayerMoveListener;
import arda.morkoc.foxHomes.managers.ConfigManager;
import arda.morkoc.foxHomes.managers.LangManager;
import arda.morkoc.foxHomes.managers.TeleportManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class FoxHomes extends JavaPlugin {

    private static FoxHomes instance;
    private static FoxHomesAPI api;

    private ConfigManager configManager;
    private LangManager langManager;
    private DatabaseManager databaseManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        langManager = new LangManager(this);
        databaseManager = new DatabaseManager(this);
        teleportManager = new TeleportManager(this);

        databaseManager.connect();
        setupAPI();
        registerCommands();
        registerListeners();

        getLogger().info("FoxHomes has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregister(FoxHomesAPI.class, api);
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("FoxHomes has been disabled.");
    }

    private void setupAPI() {
        api = new APIImplementation(this);
        getServer().getServicesManager().register(FoxHomesAPI.class, api, this, ServicePriority.Normal);
        getLogger().info("FoxHomes API has been registered.");
    }

    private void registerCommands() {
        getCommand("sethome").setExecutor(new SetHomeCommand(this));

        DelHomeCommand delHomeCommand = new DelHomeCommand(this);
        getCommand("delhome").setExecutor(delHomeCommand);
        getCommand("delhome").setTabCompleter(delHomeCommand);

        HomeCommand homeCommand = new HomeCommand(this);
        getCommand("home").setExecutor(homeCommand);
        getCommand("home").setTabCompleter(homeCommand);

        getCommand("homelist").setExecutor(new HomeListCommand(this));

        FoxHomesAdminCommand adminCommand = new FoxHomesAdminCommand(this);
        getCommand("foxhomes").setExecutor(adminCommand);
        getCommand("foxhomes").setTabCompleter(adminCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
    }

    public static FoxHomes getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
    public LangManager getLangManager() {
        return langManager;
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
    public static FoxHomesAPI getApi() {
        return api;
    }
}
