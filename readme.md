# 🦊 FoxHomes

A modern, fast, and developer-friendly homes plugin for Spigot & Paper servers. FoxHomes is built from the ground up with performance and flexibility in mind, offering extensive configuration and a powerful API for developers.

-----

## ✨ Features

- **Performance-Focused:** All database operations are asynchronous, ensuring your server never experiences lag from this plugin.
- **Broad Version Support:** Works flawlessly from Spigot/Paper 1.8.8 to the latest Minecraft version.
- **Hex Color Support:** Use modern `<SOLID:HEX>` or `<GRADIENT:HEX>` color codes in all messages for a vibrant look on 1.16+ servers.  
  Examples:  
  <GRADIENT:2C08BA>Cool string with a gradient</GRADIENT:028A97>  
  <RAINBOW1>THIS IS A REALLY COOL Rainbow</RAINBOW>  
  <RAINBOW100>THIS IS A REALLY COOL Rainbow</RAINBOW>  
  <SOLID:FF0080>Cool RGB SUPPORT</SOLID>
- **Database Options:** Supports both **SQLite** for simple, file-based storage and **MySQL** for advanced, large-scale networks.
- **Powerful Developer API:** Includes a cancellable pre-teleport event, chunk-based home lookups, and more for deep integration.
- **Fully Customizable:** All messages, teleport delays, home limits, and more can be configured to your liking.
- **Multi-Language Support:** Easily translate the plugin using the provided `en.yml` and `tr.yml` language files.
- **Advanced Permission System:** Assign custom home limits, teleport cooldowns, and bypass permissions to different players or groups.

-----

## 🚀 Installation

1. Download the latest version from the [Polymart page](https://polymart.org/product/8473/foxhomes-modern-homes) or [GitHub Releases](https://github.com/benardamorkoc/foxhomes/releases).
2. Place the `FoxHomes-1.0.0.jar` file into your server's `plugins` folder.
3. Restart your server. The default configuration files (`config.yml` and `lang/`) will be generated.
4. Edit the configuration to your liking and use `/foxhomes reload` to apply the changes.

-----

## 🎥 Video Showcase

[![FoxHomes Showcase](https://img.youtube.com/vi/ScdO4Gv7pis/hqdefault.jpg)](https://www.youtube.com/watch?v=ScdO4Gv7pis "Watch on YouTube")

-----

## 💻 Commands & Permissions
<details>
<summary><strong>Click to expand the full list of commands and permissions</strong></summary>

### Player Commands

| Command | Description | Permission |
|---|---|---|
| `/sethome <name>` | Sets a new home at your current location. | `foxhomes.sethome` |
| `/delhome <name>` | Deletes one of your homes. | `foxhomes.delhome` |
| `/home <name>` | Teleports to one of your homes. | `foxhomes.home` |
| `/homelist` | Lists all of your available homes. | `foxhomes.homelist` |

### Admin Commands

| Command | Description | Permission |
|---|---|---|
| `/foxhomes reload` | Reloads the plugin's configuration. | `foxhomes.admin` |
| `/foxhomes list <player>` | Lists the homes of a specific player. | `foxhomes.admin` |
| `/foxhomes delhome <player> <name>` | Deletes a specific home of a player. | `foxhomes.admin` |

### All Permissions

```yaml
foxhomes.sethome: Allows using the /sethome command.
foxhomes.delhome: Allows using the /delhome command.
foxhomes.home: Allows using the /home command.
foxhomes.homelist: Allows using the /homelist command.
foxhomes.admin: Grants access to all /foxhomes admin commands.
foxhomes.unlimited: Allows setting an unlimited number of homes.
foxhomes.maxhomes.<number>: Sets a specific home limit (e.g., foxhomes.maxhomes.10).
foxhomes.bypass.move: Bypasses teleport cancellation on movement.
foxhomes.bypass.cooldown: Bypasses the teleport delay entirely.
foxhomes.cooldown.<seconds>: Sets a custom teleport delay for a player/group.
