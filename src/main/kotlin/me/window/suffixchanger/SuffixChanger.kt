package me.window.suffixchanger

import net.luckperms.api.LuckPerms
import net.luckperms.api.model.user.User
import net.luckperms.api.node.Node
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


class SuffixChanger : JavaPlugin(), CommandExecutor {
    companion object {
        var api: LuckPerms? = null
        lateinit var oConfig: FileConfiguration

        fun addPermission(user: User, permission: String) {
            // Add the permission
            user.data().add(Node.builder(permission).build())

            // Now we need to save changes.
            api!!.userManager.saveUser(user)
        }

        fun removePermission(user: User, permission: String) {
            // Add the permission
            user.data().remove(Node.builder(permission).build())

            // Now we need to save changes.
            api!!.userManager.saveUser(user)
        }

        fun getUser(player: Player): User {
            return api!!.getPlayerAdapter(Player::class.java).getUser(player)
        }
    }

    override fun onEnable() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            val provider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
            if (provider != null) {
                oConfig = config
                config.addDefault("watermark", true)
                config.addDefault("obfuscate", true)
                config.options().copyDefaults(true);
                saveConfig();
                api = provider.provider
                this.getCommand("suffix")!!.setExecutor(this)
            } else {
                logger.warning("Could not find LuckPerms! This plugin is required.");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            logger.warning("Could not find LuckPerms! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if(sender is Player) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this) {
                SuffixGui.inventory(sender)
            }
        }

        return true
    }
}
