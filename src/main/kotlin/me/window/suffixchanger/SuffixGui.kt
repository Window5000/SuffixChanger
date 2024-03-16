package me.window.suffixchanger

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.model.group.Group
import net.projecttl.inventory.gui
import net.projecttl.inventory.util.InventoryType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack


object SuffixGui {
    fun inventory(player: Player) {
        val suffixes = ArrayList<String>()

        val track = SuffixChanger.api!!.trackManager.getTrack("suffix")
        for (group in track!!.groups) {
            if(player.hasPermission("sufx.$group")) {
                suffixes.add(0, group)
            } else {
                suffixes.add(group)
            }
        }

        fun getSuffix(roup: String): String {
            val group: Group = SuffixChanger.api!!.groupManager.getGroup(roup)!!
            return "&r&f" + group.cachedData.metaData.suffix;
        }

        fun createItem(suffix: Component, suffixName: String): ItemStack {
            if (player.hasPermission("sufx.$suffixName")) {
                val item = ItemStack(Material.NAME_TAG)
                val meta = item.itemMeta
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
                meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(suffix).strip())))
                item.setItemMeta(meta)
                item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                return item
            } else {
                val item = ItemStack(Material.NAME_TAG)
                val meta = item.itemMeta
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                if(SuffixChanger.oConfig.getBoolean("obfuscate"))
                    meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(suffix).strip()).decorate(TextDecoration.OBFUSCATED)))
                else
                    meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(suffix).strip())))
                item.setItemMeta(meta)
                return item
            }
        }

        player.gui(Component.text("Test" + "GUI"), InventoryType.CHEST_54) {
            for ((i, suffix) in suffixes.withIndex()) {
                slot(if (i >= 49) i + 1 else i, createItem(LegacyComponentSerializer.legacyAmpersand().deserialize(getSuffix(suffix)), suffix)) {
                    if (player.hasPermission("sufx.$suffix")) {
                        for (suffix2 in SuffixChanger.api!!.trackManager.getTrack("suffix")!!.groups) {
                            SuffixChanger.removePermission(SuffixChanger.getUser(player), "group.$suffix2")
                        }
                        SuffixChanger.addPermission(SuffixChanger.getUser(player), "group.$suffix")
                        player.sendMessage(Component.text("Your suffix is now ").color(NamedTextColor.AQUA)
                                .append(MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(getSuffix(suffix))).strip())))
                    }
                }
            }

            val name = Bukkit.getPluginManager().getPlugin("SuffixChanger")?.pluginMeta?.name
            val version = Bukkit.getPluginManager().getPlugin("SuffixChanger")?.pluginMeta?.version
            val authors = Bukkit.getPluginManager().getPlugin("SuffixChanger")?.pluginMeta?.authors

            val item = ItemStack(Material.BARRIER)
            val meta = item.itemMeta
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            meta.displayName(Component.text("Close", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
            val lore = ArrayList<Component>()
            lore.add(Component.text("$name v$version", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false))
            if (authors != null) {
                lore.add(Component.text("Made by:", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false))
                for (author in authors) {
                    lore.add(Component.text("- $author", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                }
            }
            if(SuffixChanger.oConfig.getBoolean("watermark")) meta.lore(lore)
            item.setItemMeta(meta)
            slot(49, item) {
                close()
            }
        }
    }
}