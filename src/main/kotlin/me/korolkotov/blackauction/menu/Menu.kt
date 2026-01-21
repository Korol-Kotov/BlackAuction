package me.korolkotov.blackauction.menu

import me.korolkotov.blackauction.menu.button.Button
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class Menu(
    title: Component,
    val size: Int
) : InventoryHolder {
    private val buttons = mutableListOf<Button>()
    protected val inv = Bukkit.createInventory(this, size, title)

    private var initialized = false

    fun init(player: Player) {
        buttons.clear()
        initButtons(player)

        for (button in buttons) {
            if (button.getSlots().isEmpty()) continue

            for (slot in button.getSlots()) {
                inv.setItem(slot, button.getItem(slot))
            }
        }
    }

    override fun getInventory(): Inventory = inv

    fun update(slot: Int) {
        val button = buttons.firstOrNull { it.getSlots().contains(slot) } ?: return
        inv.setItem(slot, button.getItem(slot))
    }

    fun update() {
        for (i in 0..size) update(i)
    }

    fun onClick(event: InventoryClickEvent) {
        val clicked = inv.getItem(event.slot)
        if (clicked == null || clicked.type.isEmpty) return

        val button = buttons.firstOrNull { button -> button.getSlots().contains(event.slot) } ?: return
        button.onClick(Button.ClickData.from(button, event))
    }

    fun open(player: Player) {
        if (!initialized) {
            init(player)
            initialized = true
        }
        player.openInventory(inv)
    }

    abstract fun canDrag(): Boolean
    abstract fun initButtons(player: Player)

    open fun onClose(player: Player) {}

    protected fun addButton(button: Button) {
        if (buttons.contains(button)) return
        if (button.getSlots().isEmpty()) return

        buttons.add(button)
    }
}