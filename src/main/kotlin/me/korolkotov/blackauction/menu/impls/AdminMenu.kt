package me.korolkotov.blackauction.menu.impls

import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.CloseButton
import me.korolkotov.blackauction.menu.button.MenuButton
import me.korolkotov.blackauction.menu.button.SimpleButton
import me.korolkotov.blackauction.util.ItemBuilder
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.PlayerUtil
import me.korolkotov.blackauction.util.format
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class AdminMenu : Menu("admin-menu") {
    private val auctionManager get() = LoadManager.getInstance(AuctionManager::class.java)

    override fun canDrag(slot: Int) = false

    override fun initButtons(player: Player) {
        config.items.filter { it.getMenu() != null }.forEach { item ->
            val menu = getFromId(item.getMenu()!!, player) ?: return@forEach
            addButton(MenuButton(
                item.getItem()!!,
                item.getSlots(),
                menu
            ))
        }

        val close = config.getItem("close")
        addButton(CloseButton(
            close.getItem()!!,
            close.getSlots()
        ))

        for (id in 0..<ConfigManager.instance.config.auction.general.maxLots) {
            val slot = config.getItem("lot").getSlots()[id]
            addButton(SimpleButton(
                { slot ->
                    val lotSlot = config.getItem("lot").getSlots().indexOf(slot)
                    val lot = auctionManager.auctionCache.get(lotSlot)
                    if (lot != null) getLotItem(lot) else getInactiveLotItem()
                },
                listOf(slot)
            ) { data ->
                val lotSlot = config.getItem("lot").getSlots().indexOf(data.slot)
                val lot = auctionManager.auctionCache.get(lotSlot) ?: return@SimpleButton
                val menu = AdminLotMenu(lot)
                menu.open(data.player)
            })
        }

        val create = config.getItem("create")
        addButton(SimpleButton(
            { create.getItem()!! },
            create.getSlots()
        ) { data ->
            val am = auctionManager
            if (!am.canCreateLot()) {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.maxLots)
                return@SimpleButton
            }

            val lot = am.createLot(data.player)
            val menu = AdminLotMenu(lot)
            menu.open(data.player)
        })
    }

    private fun getLotItem(lot: Lot): ItemStack {
        val builder = ItemBuilder(lot.item.clone())
        val replacements = mapOf(
            "%start_price%" to lot.startPrice.toString(),
            "%creator%" to PlayerUtil.resolvePlayerName(lot.createdBy),
            "%start_date%" to lot.startTime.format(ConfigManager.instance.config.auction.general.dateFormat),
            "%end_date%" to lot.endTime.format(ConfigManager.instance.config.auction.general.dateFormat)
        )
        val lore = MessageService.format(config.getItem("lot").getLore(), replacements)
        return builder.lore(lore).build()
    }

    private fun getInactiveLotItem(): ItemStack {
        return config.getItem("inactive-lot").getItem()!!
    }
}