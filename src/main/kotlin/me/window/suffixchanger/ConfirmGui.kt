package me.window.suffixchanger

import me.window.suffixchanger.SuffixChanger.Companion.api
import me.window.suffixchanger.SuffixChanger.Companion.toSuffixComponent
import me.window.suffixchanger.SuffixChanger.Companion.unitalic
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.luckperms.api.node.matcher.NodeMatcher
import net.luckperms.api.node.types.InheritanceNode
import net.luckperms.api.node.types.PermissionNode
import net.luckperms.api.node.types.SuffixNode
import net.projecttl.inventory.gui
import net.projecttl.inventory.util.InventoryType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ConfirmGui {
    fun inventory(player: Player, suffixRank: String, action: ConfirmAction, newSuffix: String = "") {
        val suffix = SuffixChanger.getSuffix(suffixRank).toSuffixComponent()
        val actionComponent = Component.text(when (action) {
            ConfirmAction.REMOVE -> "Confirm Removing $suffixRank"
            ConfirmAction.CLEAR -> "Confirm Clearing $suffixRank"
            ConfirmAction.EDIT -> "Confirm Editing $suffixRank"
        }, NamedTextColor.RED)

        player.gui(actionComponent, InventoryType.CHEST_9) {
            val item1 = ItemStack.of(Material.RED_STAINED_GLASS_PANE)
            val meta1 = item1.itemMeta
            meta1.displayName(Component.text("Cancel", NamedTextColor.RED).unitalic().decoration(TextDecoration.BOLD, true))
            item1.itemMeta = meta1
            slot(1, item1) {
                close()
            }
            val item2 = ItemStack.of(Material.GREEN_STAINED_GLASS_PANE)
            val meta2 = item1.itemMeta
            meta2.displayName(Component.text("Confirm", NamedTextColor.GREEN).unitalic().decoration(TextDecoration.BOLD, true))
            meta2.lore(listOf(
                Component.text("Warning! This action can not be reversed!", NamedTextColor.RED).unitalic()
            ))
            item2.itemMeta = meta2
            slot(7, item2) {
                when (action) {
                    ConfirmAction.REMOVE -> {
                        val userManager = SuffixChanger.api.userManager
                        userManager.searchAll(NodeMatcher.key(InheritanceNode.builder(suffixRank).build())).join().keys.forEach {
                            val user = if (userManager.isLoaded(it)) userManager.getUser(it)!! else userManager.loadUser(it).join()!!
                            user.data().remove(InheritanceNode.builder(suffixRank).build())
                            userManager.saveUser(user);
                        }
                        SuffixChanger.api.trackManager.getTrack(SuffixChanger.oConfig.getString("track") ?: "suffixes")
                            ?.removeGroup(suffixRank)
                        SuffixChanger.api.groupManager.deleteGroup(SuffixChanger.api.groupManager.getGroup(suffixRank)!!).join()
                        api.trackManager.saveTrack(api.trackManager.getTrack(SuffixChanger.oConfig.getString("track")?: "suffixes")!!)
                    }
                    ConfirmAction.CLEAR -> {
                        val userManager = SuffixChanger.api.userManager
                        userManager.searchAll(NodeMatcher.key(PermissionNode.builder("suffix.$suffixRank").build())).join().keys.forEach {
                            val user = if (userManager.isLoaded(it)) userManager.getUser(it)!! else userManager.loadUser(it).join()!!
                            user.data().remove(PermissionNode.builder("suffix.$suffixRank").build())
                            userManager.saveUser(user);
                        }
                        userManager.searchAll(NodeMatcher.key(InheritanceNode.builder(suffixRank).build())).join().keys.forEach {
                            val user = if (userManager.isLoaded(it)) userManager.getUser(it)!! else userManager.loadUser(it).join()!!
                            user.data().remove(InheritanceNode.builder(suffixRank).build())
                            userManager.saveUser(user);
                        }
                    }
                    ConfirmAction.EDIT -> {
                        api.groupManager.modifyGroup(suffixRank) {
                            it.data().remove(SuffixNode.builder(SuffixChanger.getRawSuffix(suffixRank),
                                api.groupManager.getGroup(suffixRank)?.weight?.orElse(3)?.toInt() ?: 3
                            ).build())
                            it.data().add(SuffixNode.builder(newSuffix,
                                api.groupManager.getGroup(suffixRank)?.weight?.orElse(3)?.toInt() ?: 3
                            ).build())
                        }.join()
                    }
                }
                close()
            }

            val item3 = ItemStack.of(Material.BLUE_STAINED_GLASS_PANE)
            val meta3 = item1.itemMeta
            meta3.displayName(suffix)
            if(action != ConfirmAction.EDIT) {
                meta3.lore(listOf(
                    Component.text("This is currently being used by ${SuffixChanger.api.userManager.searchAll(
                        NodeMatcher.key(InheritanceNode.builder(suffixRank).build())).join().size} player(s)", NamedTextColor.GRAY).unitalic(),
                    Component.text("${SuffixChanger.api.userManager.searchAll(
                        NodeMatcher.key(PermissionNode.builder("suffix.$suffixRank").build())).join().size} player(s) currently have access to this suffix", NamedTextColor.GRAY).unitalic()
                ))
            } else {
                meta3.lore(listOf(
                    Component.text("This is currently being used by ${SuffixChanger.api.userManager.searchAll(
                        NodeMatcher.key(InheritanceNode.builder(suffixRank).build())).join().size} player(s)", NamedTextColor.GRAY).unitalic(),
                    Component.text("${SuffixChanger.api.userManager.searchAll(
                        NodeMatcher.key(PermissionNode.builder("suffix.$suffixRank").build())).join().size} player(s) currently have access to this suffix", NamedTextColor.GRAY).unitalic(),
                    Component.text("From:", NamedTextColor.WHITE).unitalic(),
                    suffix,
                    Component.text("To:", NamedTextColor.WHITE).unitalic(),
                    newSuffix.toSuffixComponent()
                ))
            }
            item3.itemMeta = meta3
            slot(4, item3)
        }
    }

    enum class ConfirmAction {
        REMOVE,
        CLEAR,
        EDIT
    }
}