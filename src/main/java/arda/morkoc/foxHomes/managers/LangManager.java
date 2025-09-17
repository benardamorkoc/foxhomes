package arda.morkoc.foxHomes.managers;

import arda.morkoc.foxHomes.FoxHomes;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangManager {

    private final FoxHomes plugin;
    private FileConfiguration langConfig;

    private final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private Method ofMethod;

    public LangManager(FoxHomes plugin) {
        this.plugin = plugin;
        loadLangFile();

        if (FoxHomes.isHexColorSupported()) {
            try {
                this.ofMethod = net.md_5.bungee.api.ChatColor.class.getMethod("of", String.class);
            } catch (NoSuchMethodException e) {
                plugin.getLogger().log(Level.WARNING, "Could not cache ChatColor.of method despite hex support being enabled.", e);
                this.ofMethod = null;
            }
        }
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
        if (text == null) {
            return "";
        }

        if (FoxHomes.isHexColorSupported() && this.ofMethod != null) {
            Matcher matcher = HEX_PATTERN.matcher(text);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                try {
                    Object chatColorObject = this.ofMethod.invoke(null, "#" + matcher.group(1));
                    matcher.appendReplacement(buffer, chatColorObject.toString());
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to apply hex color via reflection", e);
                }
            }
            text = matcher.appendTail(buffer).toString();
        } else {
            text = HEX_PATTERN.matcher(text).replaceAll("");
        }

        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
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