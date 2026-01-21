package me.korolkotov.blackauction.menu

import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.load.LoadManagerInterface
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.scheduler.BukkitRunnable

class MenuManager : LoadManagerInterface<MenuManager>, Listener {
    override fun getInstance() = this

    override fun initialize() {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val clicked = event.clickedInventory ?: return
        val top = event.view.topInventory.holder
        if (top is Menu) {
            if (clicked.holder is Menu) top.onClick(event)
            if (!top.canDrag()) event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val top = event.view.topInventory.holder
        if (top is Menu) {
            object : BukkitRunnable() {
                override fun run() {
                    top.onClose(event.player as Player)
                }
            }.runTaskLater(Main.instance, 1)
        }
    }
}