package me.korolkotov.blackauction.menu.impls

import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.ItemButton
import me.korolkotov.blackauction.menu.button.SimpleButton
import me.korolkotov.blackauction.util.ItemBuilder
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.PlayerUtil
import me.korolkotov.blackauction.util.asComponent
import me.korolkotov.blackauction.util.diffFormat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class LotMenu(
    private val lot: Lot
) : Menu("lot-menu") {
    override fun createInventory(): Inventory {
        val itemName = LegacyComponentSerializer.legacySection().serialize(lot.item.itemMeta.displayName() ?: Component.translatable(lot.item.translationKey()))
        val title = MessageService.format(config.title, mapOf("%item_name%" to itemName))
        return Bukkit.createInventory(this, config.size, title.asComponent())
    }

    override fun canDrag(slot: Int) = false

    override fun initButtons(player: Player) {
        addButton(ItemButton(lot.item.clone(), config.getItem("lot").getSlots()))

        val info = config.getItem("info")
        addButton(SimpleButton(
            { getInfoItem() },
            info.getSlots(),
            {}
        ))

        val bidMin = config.getItem("bid-min")
        val name = LegacyComponentSerializer.legacyAmpersand().serialize(bidMin.getItem()!!.itemMeta.displayName() ?: Component.translatable(bidMin.getItem()!!.translationKey()))
        addButton(SimpleButton(
            { ItemBuilder(bidMin.getItem()!!).name(MessageService.format(name, mapOf("%min_step%" to lot.minStep.toString()))).build() },
            bidMin.getSlots()
        ) { data ->
            TODO("do bid")
        })
    }

    private fun getInfoItem(): ItemStack {
        val leader = if (lot.leader == null) "Нет ставок" else PlayerUtil.resolvePlayerName(lot.leader!!)
        val replacements = mapOf(
            "%current_bid%" to if (lot.currentBid > 0) lot.currentBid.toString() else "Нет ставок",
            "%min_step%" to lot.minStep.toString(),
            "%leader%" to leader,
            "%time_left%" to lot.endTime.diffFormat(lot.startTime, ConfigManager.instance.config.auction.general.timeFormat)
        )
        val item = config.getItem("info")
        val builder = ItemBuilder(item.getItem()!!.clone())
        val lore = MessageService.format(item.getLore(), replacements)
        return builder.lore(lore).build()
    }
}