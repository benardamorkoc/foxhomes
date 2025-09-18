package arda.morkoc.foxHomes.managers;

import arda.morkoc.foxHomes.FoxHomes;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandOverrideManager {

    private final FoxHomes plugin;

    public CommandOverrideManager(FoxHomes plugin) {
        this.plugin = plugin;
    }

    public void registerOverrides() {
        try {
            SimpleCommandMap commandMap = getCommandMap();
            if (commandMap == null) {
                plugin.getLogger().severe("Could not get CommandMap. Command overriding is disabled.");
                return;
            }

            Map<String, Command> knownCommands = getKnownCommands(commandMap);
            if (knownCommands == null) {
                plugin.getLogger().severe("Could not get KnownCommands map. Command overriding is disabled.");
                return;
            }

            overrideCommands(commandMap, knownCommands, plugin.getConfigManager().getHomeAliases(), "home");
            overrideCommands(commandMap, knownCommands, plugin.getConfigManager().getSetHomeAliases(), "sethome");
            overrideCommands(commandMap, knownCommands, plugin.getConfigManager().getDelHomeAliases(), "delhome");
            overrideCommands(commandMap, knownCommands, plugin.getConfigManager().getHomeListAliases(), "homelist");

        } catch (Exception e) {
            plugin.getLogger().severe("An error occurred while overriding commands: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void overrideCommands(SimpleCommandMap commandMap, Map<String, Command> knownCommands, List<String> aliases, String ownCommandName) {
        PluginCommand ownCommand = plugin.getCommand(ownCommandName);
        if (ownCommand == null) return;

        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(ownCommandName) || alias.equalsIgnoreCase("foxhomes:" + ownCommandName)) {
                continue;
            }

            List<String> commandsToRemove = new ArrayList<>();
            for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(alias)) {
                    commandsToRemove.add(entry.getKey());
                    Command cmd = entry.getValue();
                    if (cmd instanceof PluginCommand) {
                        Plugin owner = ((PluginCommand) cmd).getPlugin();
                        if (owner != null) {
                            plugin.getLogger().info("Overrode command '/" + alias + "' from plugin '" + owner.getName() + "'.");
                        }
                    }
                }
            }

            for (String cmdName : commandsToRemove) {
                knownCommands.remove(cmdName);
            }

            commandMap.register(alias, plugin.getName().toLowerCase(), ownCommand);
        }
    }

    private SimpleCommandMap getCommandMap() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                return (SimpleCommandMap) field.get(Bukkit.getPluginManager());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> getKnownCommands(SimpleCommandMap commandMap) {
        try {
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);
            return (Map<String, Command>) field.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}