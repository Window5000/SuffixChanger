package me.window.suffixchanger

import me.window.suffixchanger.SuffixChanger.Companion.api
import me.window.suffixchanger.SuffixChanger.Companion.stripSuffix
import me.window.suffixchanger.SuffixChanger.Companion.toSuffixComponent
import me.window.suffixchanger.SuffixChanger.Companion.unitalic
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.node.types.DisplayNameNode
import net.luckperms.api.node.types.SuffixNode
import net.luckperms.api.node.types.WeightNode
import net.projecttl.inventory.gui
import net.projecttl.inventory.util.InventoryType
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.function.BiFunction

object AddSuffixGui {
    lateinit var plugin: SuffixChanger

    data class NewRankData(
        var rankName: String? = null,
        var suffix: String? = null,
        var weight: Int = SuffixChanger.oConfig.getInt("group_weight")
    )

    private val data: HashMap<Player, NewRankData> = HashMap<Player, NewRankData>()

    fun getNameItemStack(player: Player): ItemStack {
        val stack = ItemStack.of(Material.SPRUCE_SIGN)
        val meta = stack.itemMeta
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        if (data[player]?.rankName == null) {
            meta.displayName(Component.text("Change Name", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
            meta.setEnchantmentGlintOverride(true)
        } else {
            meta.displayName(Component.text(data[player]?.rankName!!).decoration(TextDecoration.ITALIC, false))
        }
        stack.setItemMeta(meta)
        return stack
    }

    fun getSuffixItemStack(player: Player): ItemStack {
        val stack = ItemStack.of(Material.NAME_TAG)
        val meta = stack.itemMeta
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        if (data[player]?.suffix == null) {
            meta.displayName(
                Component.text("Change Suffix", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
            )
            meta.setEnchantmentGlintOverride(true)
        } else {
            meta.displayName(
                Component.empty().decoration(TextDecoration.ITALIC, false).append(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(data[player]?.suffix!!.stripSuffix())
                )
            )
        }
        stack.setItemMeta(meta)
        return stack
    }

    fun getWeightItemStack(player: Player): ItemStack {
        val stack = ItemStack.of(Material.ANVIL)
        val meta = stack.itemMeta
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.displayName(Component.text(data[player]?.weight ?: 3).decoration(TextDecoration.ITALIC, false))
        stack.setItemMeta(meta)
        return stack
    }

    private fun getConfirmItemStack(player: Player): ItemStack {
        if (data[player]?.rankName == null || data[player]?.suffix == null) {
            val stack = ItemStack.of(Material.RED_STAINED_GLASS_PANE)
            val meta = stack.itemMeta

            meta.displayName(
                Component.text("Confirm Suffix", NamedTextColor.RED).decoration(TextDecoration.BOLD, true).unitalic()
            )
            val lore = ArrayList<Component>()
            if (data[player]?.rankName == null) {
                lore.add(Component.text("You need to select a name for the rank.", NamedTextColor.RED).unitalic())
            }
            if (data[player]?.suffix == null) {
                lore.add(Component.text("You need to select a suffix.", NamedTextColor.RED).unitalic())
            }
            meta.lore(lore)

            stack.itemMeta = meta
            return stack
        } else if (SuffixChanger.api.trackManager.getTrack(
                SuffixChanger.oConfig.getString("track") ?: "suffixes"
            )?.groups!!.any { it == data[player]?.rankName }
        ) {
            val stack = ItemStack.of(Material.YELLOW_STAINED_GLASS_PANE)
            val meta = stack.itemMeta

            meta.displayName(
                Component.text("Confirm Suffix", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true).unitalic()
            )
            val lore = ArrayList<Component>()
            lore.add(Component.text("This rank already exists!", NamedTextColor.RED).unitalic())
            meta.lore(lore)

            stack.itemMeta = meta
            return stack
        } else {
            val stack = ItemStack.of(Material.GREEN_STAINED_GLASS_PANE)
            val meta = stack.itemMeta

            meta.displayName(
                Component.text("Confirm Suffix", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true).unitalic()
            )
            meta.setEnchantmentGlintOverride(true)
            val lore = ArrayList<Component>()
            lore.add(
                Component.text("Rank Name: ", NamedTextColor.AQUA).unitalic()
                    .append(Component.text(data[player]?.rankName!!, NamedTextColor.WHITE))
            )
            lore.add(
                Component.text("Suffix: ", NamedTextColor.AQUA).unitalic().append(
                    Component.text(
                        "",
                        NamedTextColor.WHITE
                    )
                ).append(LegacyComponentSerializer.legacyAmpersand().deserialize(data[player]?.suffix!!.stripSuffix()))
            )
            lore.add(
                Component.text("Weight: ", NamedTextColor.AQUA).unitalic()
                    .append(Component.text(data[player]?.weight.toString(), NamedTextColor.WHITE))
            )
            meta.lore(lore)

            stack.itemMeta = meta
            return stack
        }
    }

    fun inventory(player: Player, isDoing: Boolean = false) {
        if (!isDoing) data[player] = NewRankData()
        player.gui(Component.text("Add Suffix"), InventoryType.CHEST_9) {
            slot(1, getNameItemStack(player)) {
                selectName(player)
            }
            slot(3, getSuffixItemStack(player)) {
                selectSuffix(player)
            }
            slot(5, getWeightItemStack(player)) {
                selectWeight(player)
            }

            slot(7, getConfirmItemStack(player)) {
                if (!player.hasPermission("suffixchanger.admin")) {
                    player.sendMessage(Bukkit.permissionMessage())
                } else {
                    val dat = data[player]!!
                    api.groupManager.createAndLoadGroup(dat.rankName!!.lowercase()).thenRun {
                        api.groupManager.modifyGroup(dat.rankName!!.lowercase()) {
                            it.data().add(DisplayNameNode.builder("Suffix: ${dat.rankName!!.lowercase()}").build())
                            it.data().add(WeightNode.builder(dat.weight).build())
                            it.data().add(SuffixNode.builder(dat.suffix!!, 3).build())
                        }
                        api.trackManager.getTrack(SuffixChanger.oConfig.getString("track") ?: "suffixes")!!
                            .appendGroup(api.groupManager.getGroup(dat.rankName!!.lowercase())!!)
                        api.trackManager.saveTrack(
                            api.trackManager.getTrack(
                                SuffixChanger.oConfig.getString("track") ?: "suffixes"
                            )!!
                        )
                        player.sendMessage(Component.text("Successfully created group ${dat.rankName!!.lowercase()} with suffix ", NamedTextColor.WHITE).append(dat.suffix!!.toSuffixComponent()).append(
                            Component.text(" and weight ${dat.weight}", NamedTextColor.WHITE)))
                        data.remove(player)
                        player.closeInventory()
                    }
                }
            }
        }
    }

    private fun selectName(player: Player) {
        val builder = AnvilGUI.Builder()
        builder.title("Change Rank Name")
        builder.itemLeft(getNameItemStack(player))
        builder.plugin(plugin)
        if (data[player]?.rankName != null) {
            builder.text(data[player]?.rankName)
        }
        builder.onClick(BiFunction { slot, stateSnapshot ->
            if (slot != AnvilGUI.Slot.OUTPUT) {
                return@BiFunction emptyList()
            }

            if (stateSnapshot.text.isBlank()) {
                return@BiFunction emptyList()
            }

            return@BiFunction listOf(AnvilGUI.ResponseAction.run {
                data[player]?.rankName = stateSnapshot.text
                inventory(player, true)
            })
        })
        builder.open(player)
    }

    private fun selectSuffix(player: Player) {
        val builder = AnvilGUI.Builder()
        builder.title("Change Suffix")
        builder.itemLeft(getSuffixItemStack(player))
        builder.plugin(plugin)
        if (data[player]?.suffix != null) {
            builder.text(data[player]?.suffix)
        }
        builder.onClick(BiFunction { slot, stateSnapshot ->
            if (slot != AnvilGUI.Slot.OUTPUT) {
                return@BiFunction emptyList()
            }

            if (stateSnapshot.text.isBlank()) {
                return@BiFunction emptyList()
            }

            return@BiFunction listOf(AnvilGUI.ResponseAction.run {
                data[player]?.suffix = stateSnapshot.text
                inventory(player, true)
            })
        })
        builder.open(player)
    }

    private fun selectWeight(player: Player) {
        val builder = AnvilGUI.Builder()
        builder.title("Change Rank Weight")
        builder.itemLeft(getWeightItemStack(player))
        builder.plugin(plugin)
        builder.text(data[player]?.weight.toString())
        builder.onClick(BiFunction { slot, stateSnapshot ->
            if (slot != AnvilGUI.Slot.OUTPUT) {
                return@BiFunction emptyList()
            }

            if (stateSnapshot.text.isBlank()) {
                return@BiFunction emptyList()
            }

            if (stateSnapshot.text.toInt() < 0) {
                return@BiFunction emptyList()
            }

            return@BiFunction listOf(AnvilGUI.ResponseAction.run {
                data[player]?.weight = stateSnapshot.text.toInt()
                inventory(player, true)
            })
        })
        builder.open(player)
    }
}