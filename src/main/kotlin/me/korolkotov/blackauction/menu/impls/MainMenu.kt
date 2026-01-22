package me.korolkotov.blackauction.menu.impls

import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.CloseButton
import me.korolkotov.blackauction.menu.button.ItemButton
import me.korolkotov.blackauction.menu.button.MenuButton
import me.korolkotov.blackauction.menu.button.SimpleButton
import me.korolkotov.blackauction.util.ItemBuilder
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.PlayerUtil
import me.korolkotov.blackauction.util.diffFormat
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MainMenu : Menu("main-menu") {
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
                    val lot = auctionManager.cache.get(lotSlot)
                    if (lot != null) getLotItem(lot) else getInactiveLotItem()
                },
                listOf(slot)
            ) { data ->
                val lotSlot = config.getItem("lot").getSlots().indexOf(data.slot)
                val lot = auctionManager.cache.get(lotSlot) ?: return@SimpleButton
                val lotMenu = LotMenu(lot)
                lotMenu.open(data.player)
            })
        }

        val filler = config.getItem("filler")
        if (!filler.section.getBoolean("enabled")) return
        val remainSlots = (0..<inv.size) - getButtons().flatMap { it.getSlots() }.toSet()
        val fillerItem = filler.getItem()!!
        for (slot in remainSlots) addButton(ItemButton(fillerItem.clone(), listOf(slot)))
    }

    private fun getLotItem(lot: Lot): ItemStack {
        val leader = if (lot.leader == null) "Нет ставок" else PlayerUtil.resolvePlayerName(lot.leader!!)
        val replacements = mapOf(
            "%start_price%" to lot.startPrice.toString(),
            "%leader%" to leader,
            "%time%" to lot.endTime.diffFormat(lot.startTime, ConfigManager.instance.config.auction.general.timeFormat)
        )
        val builder = ItemBuilder(lot.item.clone())
        val lore = MessageService.format(config.getItem("lot").getLore(), replacements)
        return builder.lore(lore).build()
    }

    private fun getInactiveLotItem(): ItemStack {
        return config.getItem("inactive-lot").getItem()!!
    }
}