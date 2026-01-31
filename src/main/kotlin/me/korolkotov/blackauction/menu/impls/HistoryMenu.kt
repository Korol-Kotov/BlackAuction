package me.korolkotov.blackauction.menu.impls

import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.auction.model.PlayerHistory
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.economy.EconomyManager
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.MenuButton
import me.korolkotov.blackauction.menu.button.SimpleButton
import me.korolkotov.blackauction.util.ItemBuilder
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.format
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class HistoryMenu(
    private val player: Player,
    private val page: Int
) : Menu("my-history-menu") {
    val auctionManager get() = LoadManager.getInstance(AuctionManager::class.java)

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

        val historyItem = config.getItem("item")
        addButton(SimpleButton(
            { slot ->
                val history = auctionManager.getHistory(this.player)
                val index = historyItem.getSlots().indexOf(slot) + (page - 1) * historyItem.getSlots().size
                val entry = history.getOrNull(index) ?: return@SimpleButton ItemStack(Material.AIR)
                getItem(entry)
            },
            historyItem.getSlots(),
            {}
        ))

        val previousPage = config.getItem("previous-page")
        addButton(SimpleButton(
            { if (page > 1) previousPage.getItem()!! else ItemStack(Material.AIR) },
            previousPage.getSlots()
        ) { data ->
            if (inv.getItem(data.slot)?.type?.isEmpty != false) return@SimpleButton

            val menu = HistoryMenu(player, page - 1)
            menu.open(data.player)
        })

        val nextPage = config.getItem("next-page")
        addButton(SimpleButton(
            {
                val historyItem = config.getItem("item")
                val history = auctionManager.getHistory(this.player)
                if (history.size > historyItem.getSlots().size * page) nextPage.getItem()!!
                else ItemStack(Material.AIR)
            },
            nextPage.getSlots()
        ) { data ->
            if (inv.getItem(data.slot)?.type?.isEmpty != false) return@SimpleButton

            val menu = HistoryMenu(player, page + 1)
            menu.open(data.player)
        })
    }

    private fun getItem(playerHistory: PlayerHistory): ItemStack {
        val historyItem = config.getItem("item")
        val builder = ItemBuilder(ItemStack(Material.PAPER))
        val replacements = mapOf(
            "%date%" to playerHistory.wonAt.format(ConfigManager.instance.config.auction.general.dateFormat),
            "%final_price%" to EconomyManager.instance.format(playerHistory.economy, playerHistory.finalPrice),
            "%status%" to if (playerHistory.claimedAt != null) "Получен" else "Не получен"
        )
        val lore = MessageService.format(historyItem.getLore(), replacements)
        return builder.name(playerHistory.item).lore(lore).build()
    }
}