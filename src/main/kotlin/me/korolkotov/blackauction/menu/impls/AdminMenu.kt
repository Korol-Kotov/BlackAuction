package me.korolkotov.blackauction.menu.impls

import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.economy.EconomyManager
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.CloseButton
import me.korolkotov.blackauction.menu.button.MenuButton
import me.korolkotov.blackauction.menu.button.SimpleButton
import me.korolkotov.blackauction.util.ItemBuilder
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.PlayerUtil
import me.korolkotov.blackauction.util.format
import org.bukkit.Material
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

        val lot = config.getItem("lot")
        addButton(SimpleButton(
            { slot ->
                val lotSlot = lot.getSlots().indexOf(slot)
                val lot = auctionManager.auctionCache.get(lotSlot)
                if (lot != null) getLotItem(lot) else getInactiveLotItem()
            },
            lot.getSlots()
        ) { data ->
            val lotSlot = lot.getSlots().indexOf(data.slot)
            val lot = auctionManager.auctionCache.get(lotSlot) ?: return@SimpleButton
            if (data.clickType.isLeftClick) {
                val menu = AdminLotMenu(lot)
                menu.open(data.player)
            } else if (data.clickType.isRightClick) {
                val menu = AdminLotCancelMenu(lot)
                menu.open(data.player)
            }
        })

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
        if (lot.item.type.isEmpty) return ItemStack(Material.AIR)
        val builder = ItemBuilder(lot.item.clone())
        val replacements = mapOf(
            "%start_price%" to EconomyManager.instance.format(lot.economy, lot.startPrice),
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