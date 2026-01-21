package me.korolkotov.blackauction.menu.button

import me.korolkotov.blackauction.menu.Menu
import org.bukkit.inventory.ItemStack

class MenuButton(
    private val itemStack: ItemStack,
    private val slots: List<Int>,
    private val menu: Menu
) : Button {
    override fun getSlots() = slots.toList()
    override fun getItem(slot: Int) = itemStack.clone()
    override fun onClick(data: Button.ClickData) = menu.open(data.player)
}