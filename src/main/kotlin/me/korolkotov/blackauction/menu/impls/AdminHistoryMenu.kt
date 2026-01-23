package me.korolkotov.blackauction.menu.impls

import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.auction.model.LotHistory
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.MenuButton
import me.korolkotov.blackauction.menu.button.SimpleButton
import me.korolkotov.blackauction.scanner.ScannerManager
import me.korolkotov.blackauction.util.ItemBuilder
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.format
import me.korolkotov.blackauction.util.getName
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class AdminHistoryMenu(
    private val page: Int,
    private val player: OfflinePlayer? = null
) : Menu("admin-history-menu") {
    val auctionManager get() = LoadManager.getInstance(AuctionManager::class.java)
    val scanner get() = LoadManager.getInstance(ScannerManager::class.java)

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
                val history = auctionManager.lotHistoryCache.getHistory(this.player)
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

            val menu = AdminHistoryMenu(page - 1, this.player)
            menu.open(data.player)
        })

        val nextPage = config.getItem("next-page")
        addButton(SimpleButton(
            {
                val historyItem = config.getItem("item")
                val history = auctionManager.lotHistoryCache.getHistory(this.player)
                if (history.size > historyItem.getSlots().size * page) nextPage.getItem()!!
                else ItemStack(Material.AIR)
            },
            nextPage.getSlots()
        ) { data ->
            if (inv.getItem(data.slot)?.type?.isEmpty != false) return@SimpleButton

            val menu = AdminHistoryMenu(page + 1, this.player)
            menu.open(data.player)
        })

        val filterPlayer = config.getItem("filter-player")
        addButton(SimpleButton(
            { filterPlayer.getItem()!! },
            filterPlayer.getSlots()
        ) { data ->
            MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.scannerConfig.specifyPlayer)
            data.player.closeInventory()
            scanner.waitFor(data.player, 20L * 30) { message ->
                val player = Bukkit.getOfflinePlayerIfCached(message)
                if (player == null) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.playerNotFound)
                    return@waitFor
                }

                val menu = AdminHistoryMenu(1, player)
                menu.open(data.player)
            }
        })
    }

    private fun getItem(entry: LotHistory): ItemStack {
        val historyItem = config.getItem("item")
        val builder = ItemBuilder(ItemStack(Material.PAPER))
        val player = entry.winnerName ?: "Нет победителя"
        val replacements = mapOf(
            "%player%" to player,
            "%final_price%" to (entry.finalPrice?.toString() ?: "Нет цены"),
            "%commission%" to entry.commissionTaken.toString(),
            "%date%" to entry.completedAt.format(ConfigManager.instance.config.auction.general.dateFormat)
        )
        val lore = MessageService.format(historyItem.getLore(), replacements)
        return builder.name(MessageService.format(entry.item.getName())).lore(lore).build()
    }
}