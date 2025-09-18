package arda.morkoc.foxHomes.managers;

import arda.morkoc.foxHomes.FoxHomes;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LangManager {

    private final FoxHomes plugin;
    private FileConfiguration langConfig;

    public LangManager(FoxHomes plugin) {
        this.plugin = plugin;
        loadLangFile();
    }

    public void loadLangFile() {
        String langFileName = plugin.getConfigManager().getLanguage() + ".yml";
        File langFile = new File(plugin.getDataFolder(), "lang/" + langFileName);

        if (!langFile.exists()) {
            plugin.saveResource("lang/" + langFileName, false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        try (InputStreamReader reader = new InputStreamReader(plugin.getResource("lang/" + langFileName), StandardCharsets.UTF_8)) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
            langConfig.setDefaults(defaultConfig);
            langConfig.options().copyDefaults(true);
            langConfig.save(langFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRawMessage(String path) {
        return langConfig.getString(path, "&cMessage not found: " + path);
    }

    public String translateColors(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return IridiumColorAPI.process(text);
    }

    public void sendMessage(CommandSender sender, String path, String... replacements) {
        String rawPrefix = getRawMessage("prefix");
        String rawMessage = getRawMessage(path);
        String finalMessage = rawPrefix + rawMessage;

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                finalMessage = finalMessage.replace(replacements[i], replacements[i + 1]);
            }
        }

        sender.sendMessage(translateColors(finalMessage));
    }
}
