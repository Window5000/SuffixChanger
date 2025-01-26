package me.window.suffixchanger

import me.window.suffixchanger.AddSuffixGui.plugin
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
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.function.BiFunction

object ConfirmGui {
    fun inventory(player: Player, suffixRank: String, action: ConfirmAction, newSuffix: String = "") {
        val suffix = if(action != ConfirmAction.TRACK) SuffixChanger.getSuffix(suffixRank).toSuffixComponent() else Component.text(suffixRank,
            NamedTextColor.WHITE).unitalic()
        val actionComponent = Component.text(
            when (action) {
                ConfirmAction.REMOVE -> "Confirm Removing $suffixRank"
                ConfirmAction.CLEAR -> "Confirm Clearing $suffixRank"
                ConfirmAction.EDIT -> "Confirm Editing $suffixRank"
                ConfirmAction.TRACK -> "Creating \"$suffixRank\""
            }, NamedTextColor.RED
        )

        player.gui(actionComponent, InventoryType.CHEST_9) {
            val item1 = ItemStack.of(Material.RED_STAINED_GLASS_PANE)
            val meta1 = item1.itemMeta
            meta1.displayName(
                Component.text("Cancel", NamedTextColor.RED).unitalic().decoration(TextDecoration.BOLD, true)
            )
            item1.itemMeta = meta1
            slot(1, item1) {
                close()
            }
            val item2 = ItemStack.of(Material.GREEN_STAINED_GLASS_PANE)
            val meta2 = item1.itemMeta
            meta2.displayName(
                Component.text("Confirm", NamedTextColor.GREEN).unitalic().decoration(TextDecoration.BOLD, true)
            )
            if(action != ConfirmAction.TRACK) {
                meta2.lore(
                    listOf(
                        Component.text("Warning! This action can not be reversed!", NamedTextColor.RED).unitalic()
                    )
                )
            }
            item2.itemMeta = meta2
            slot(7, item2) {
                when (action) {
                    ConfirmAction.REMOVE -> {
                        val userManager = SuffixChanger.api.userManager
                        userManager.searchAll(NodeMatcher.key(InheritanceNode.builder(suffixRank).build()))
                            .join().keys.forEach {
                            val user =
                                if (userManager.isLoaded(it)) userManager.getUser(it)!! else userManager.loadUser(it)
                                    .join()!!
                            user.data().remove(InheritanceNode.builder(suffixRank).build())
                            userManager.saveUser(user);
                        }
                        SuffixChanger.api.trackManager.getTrack(SuffixChanger.oConfig.getString("track") ?: "suffixes")
                            ?.removeGroup(suffixRank)
                        SuffixChanger.api.groupManager.deleteGroup(SuffixChanger.api.groupManager.getGroup(suffixRank)!!)
                            .join()
                        api.trackManager.saveTrack(
                            api.trackManager.getTrack(
                                SuffixChanger.oConfig.getString("track") ?: "suffixes"
                            )!!
                        )
                    }

                    ConfirmAction.CLEAR -> {
                        val userManager = SuffixChanger.api.userManager
                        userManager.searchAll(NodeMatcher.key(PermissionNode.builder("suffix.$suffixRank").build()))
                            .join().keys.forEach {
                            val user =
                                if (userManager.isLoaded(it)) userManager.getUser(it)!! else userManager.loadUser(it)
                                    .join()!!
                            user.data().remove(PermissionNode.builder("suffix.$suffixRank").build())
                            userManager.saveUser(user);
                        }
                        userManager.searchAll(NodeMatcher.key(InheritanceNode.builder(suffixRank).build()))
                            .join().keys.forEach {
                            val user =
                                if (userManager.isLoaded(it)) userManager.getUser(it)!! else userManager.loadUser(it)
                                    .join()!!
                            user.data().remove(InheritanceNode.builder(suffixRank).build())
                            userManager.saveUser(user);
                        }
                    }

                    ConfirmAction.EDIT -> {
                        api.groupManager.modifyGroup(suffixRank) {
                            it.data().remove(
                                SuffixNode.builder(
                                    SuffixChanger.getRawSuffix(suffixRank),
                                    api.groupManager.getGroup(suffixRank)?.weight?.orElse(3)?.toInt() ?: 3
                                ).build()
                            )
                            it.data().add(
                                SuffixNode.builder(
                                    newSuffix,
                                    api.groupManager.getGroup(suffixRank)?.weight?.orElse(3)?.toInt() ?: 3
                                ).build()
                            )
                        }.join()
                    }

                    ConfirmAction.TRACK -> {
                        close()
                        SuffixChanger.api.trackManager.createAndLoadTrack(suffixRank).thenRun {
                            SuffixGui.inventory(player)
                        }
                    }
                }
                close()
            }

            val item3 = ItemStack.of(Material.BLUE_STAINED_GLASS_PANE)
            val meta3 = item1.itemMeta
            meta3.displayName(suffix)
            if (action != ConfirmAction.EDIT && action != ConfirmAction.TRACK) {
                meta3.lore(
                    listOf(
                        Component.text(
                            "This is currently being used by ${
                                SuffixChanger.api.userManager.searchAll(
                                    NodeMatcher.key(InheritanceNode.builder(suffixRank).build())
                                ).join().size
                            } player(s)", NamedTextColor.GRAY
                        ).unitalic(),
                        Component.text(
                            "${
                                SuffixChanger.api.userManager.searchAll(
                                    NodeMatcher.key(PermissionNode.builder("suffix.$suffixRank").build())
                                ).join().size
                            } player(s) currently have access to this suffix", NamedTextColor.GRAY
                        ).unitalic()
                    )
                )
            } else if (action == ConfirmAction.EDIT) {
                meta3.lore(
                    listOf(
                        Component.text(
                            "This is currently being used by ${
                                SuffixChanger.api.userManager.searchAll(
                                    NodeMatcher.key(InheritanceNode.builder(suffixRank).build())
                                ).join().size
                            } player(s)", NamedTextColor.GRAY
                        ).unitalic(),
                        Component.text(
                            "${
                                SuffixChanger.api.userManager.searchAll(
                                    NodeMatcher.key(PermissionNode.builder("suffix.$suffixRank").build())
                                ).join().size
                            } player(s) currently have access to this suffix", NamedTextColor.GRAY
                        ).unitalic(),
                        Component.text("From:", NamedTextColor.WHITE).unitalic(),
                        suffix,
                        Component.text("To:", NamedTextColor.WHITE).unitalic(),
                        newSuffix.toSuffixComponent()
                    )
                )
            } else {
                meta3.lore(
                    listOf(
                        Component.text("The track $suffixRank is required, but does not exist.", NamedTextColor.GRAY).unitalic(),
                        Component.text("If you want to change the name of the track,", NamedTextColor.GRAY).unitalic(),
                        Component.text("or if you already have a track for suffixes,", NamedTextColor.GRAY).unitalic(),
                        Component.text("then change the \"track\" setting in the config.", NamedTextColor.GRAY).unitalic(),
                        Component.text("Press \"Confirm\" to create a new track named \"$suffixRank\".", NamedTextColor.GRAY).unitalic()
                    )
                )
            }
            item3.itemMeta = meta3
            slot(4, item3)
        }
    }

    fun editSuffix(player: Player, suffixRank: String) {
        val builder = AnvilGUI.Builder()
        builder.title("Change Suffix")
        val stack = ItemStack.of(Material.NAME_TAG)
        val meta = stack.itemMeta
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.displayName(SuffixChanger.getRawSuffix(suffixRank).toSuffixComponent())
        stack.setItemMeta(meta)
        builder.itemLeft(stack)
        builder.plugin(plugin)
        builder.text(SuffixChanger.getRawSuffix(suffixRank))
        builder.onClick(BiFunction { slot, stateSnapshot ->
            if (slot != AnvilGUI.Slot.OUTPUT) {
                return@BiFunction emptyList()
            }

            if (stateSnapshot.text.isBlank()) {
                return@BiFunction emptyList()
            }

            return@BiFunction listOf(AnvilGUI.ResponseAction.run {
                ConfirmGui.inventory(player, suffixRank, ConfirmAction.EDIT, stateSnapshot.text)
            })
        })
        builder.open(player)
    }

    enum class ConfirmAction {
        REMOVE,
        CLEAR,
        EDIT,
        TRACK
    }
}