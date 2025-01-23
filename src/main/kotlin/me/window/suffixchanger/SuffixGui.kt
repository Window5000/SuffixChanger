package me.window.suffixchanger

import me.window.suffixchanger.SuffixChanger.Companion.stripSuffix
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
    fun inventory(player: Player, page: Int = 0) {
        val suffixes = ArrayList<String>()

        val track = SuffixChanger.api.trackManager.getTrack(SuffixChanger.oConfig.getString("track")?: "suffixes")
        for (group in track!!.groups) {
            if(player.hasPermission("suffix.$group")) {
                suffixes.add(0, group)
            } else {
                suffixes.add(group)
            }
        }

        fun getSuffix(roup: String): String {
            val group: Group = SuffixChanger.api.groupManager.getGroup(roup)!!
            return "&r&f" + group.cachedData.metaData.suffix
        }

        fun createItem(suffix: Component, suffixName: String): ItemStack {
            if (player.hasPermission("suffix.$suffixName")) {
                val item = ItemStack(Material.NAME_TAG)
                val meta = item.itemMeta
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
                meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(suffix))
                item.setItemMeta(meta)
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1)
                return item
            } else {
                val item = ItemStack(Material.NAME_TAG)
                val meta = item.itemMeta
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                if(SuffixChanger.oConfig.getBoolean("obfuscate"))
                    meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(suffix).decorate(TextDecoration.OBFUSCATED))
                else
                    meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(suffix))
                item.setItemMeta(meta)
                return item
            }
        }

        player.gui(MiniMessage.miniMessage().deserialize(SuffixChanger.oConfig.getString("title")?: "<gradient:dark_red:red>Suffix</gradient><gradient:dark_blue:blue>Changer</gradient>"), InventoryType.CHEST_54) {
            for ((i, suffix) in suffixes.withIndex()) {
                if (i >= 45 * (page + 1)) break
                if (i < 45 * page) continue
                val newI = i - page * 45
                slot(if (newI >= 45) newI + 3 else newI, createItem(LegacyComponentSerializer.legacyAmpersand().deserialize(getSuffix(suffix).stripSuffix()), suffix)) {
                    if (player.hasPermission("suffix.$suffix")) {
                        for (suffix2 in SuffixChanger.api.trackManager.getTrack("suffixes")!!.groups) {
                            SuffixChanger.removePermission(SuffixChanger.getUser(player), "group.$suffix2")
                        }
                        SuffixChanger.addPermission(SuffixChanger.getUser(player), "group.$suffix")
                        player.sendMessage(Component.text("Your suffix is now ").color(NamedTextColor.AQUA)
                                .append(MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(getSuffix(suffix).stripSuffix())))))
                    }
                }
            }

            val name = Bukkit.getPluginManager().getPlugin("SuffixChanger")?.pluginMeta?.name
            val version = Bukkit.getPluginManager().getPlugin("SuffixChanger")?.pluginMeta?.version
            val authors = Bukkit.getPluginManager().getPlugin("SuffixChanger")?.pluginMeta?.authors

            val item = ItemStack(Material.RED_STAINED_GLASS_PANE)
            val meta = item.itemMeta
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            meta.displayName(Component.text("Close", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
            item.setItemMeta(meta)
            slot(49, item) {
                close()
            }

            val item2 = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
            val meta2 = item2.itemMeta
            meta2.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            meta2.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
            item2.setItemMeta(meta2)
            slot(48, item2) {
                if (page > 0) {
                    inventory(player, page - 1)
                }
            }

            val item3 = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
            val meta3 = item3.itemMeta
            meta3.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            meta3.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
            item3.setItemMeta(meta3)
            slot(50, item3) {
                if (suffixes.size > (page + 1) * 45) {
                    inventory(player, page + 1)
                }
            }


            val item4 = ItemStack(Material.BLUE_STAINED_GLASS_PANE)
            val meta4 = item4.itemMeta
            meta4.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            if(SuffixChanger.oConfig.getBoolean("watermark")) {
                meta4.displayName(Component.text("$name v$version", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false))
                val lore = ArrayList<Component>()
                if (authors != null) {
                    lore.add(Component.text("Made by:", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false))
                    for (author in authors) {
                        lore.add(Component.text("- $author", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                    }
                }
                meta4.lore(lore)
            } else {
                meta4.displayName(Component.empty().decoration(TextDecoration.ITALIC, false))
            }
            item4.setItemMeta(meta4)

            slot(45, item4)
            slot(46, item4)
            slot(47, item4)

            slot(51, item4)
            slot(52, item4)
            slot(53, item4)
        }
    }
}