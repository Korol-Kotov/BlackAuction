package me.korolkotov.blackauction.menu.impls

import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.auction.model.LotStatus
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.economy.EconomyManager
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.ItemButton
import me.korolkotov.blackauction.menu.button.MenuButton
import me.korolkotov.blackauction.menu.button.SimpleButton
import me.korolkotov.blackauction.scanner.ScannerManager
import me.korolkotov.blackauction.util.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class LotMenu(
    val lot: Lot
) : Menu("lot-menu") {
    val auctionManager get() = LoadManager.getInstance(AuctionManager::class.java)
    val scanner get() = LoadManager.getInstance(ScannerManager::class.java)

    override fun createInventory(): Inventory {
        val itemName = "&r" + lot.item.getName()
        val title = MessageService.format(config.title, mapOf("%item_name%" to itemName))
        return Bukkit.createInventory(this, config.size, title.asComponent())
    }

    override fun canDrag(slot: Int) = false

    override fun initButtons(player: Player) {
        config.items.filter { it.getMenu() != null }.forEach { item ->
            val menu = getFromId(item.getMenu()!!, player) ?: return@forEach
            addButton(
                MenuButton(
                    item.getItem()!!,
                    item.getSlots(),
                    menu
                )
            )
        }

        addButton(ItemButton(lot.item.clone(), config.getItem("lot").getSlots()))

        val info = config.getItem("info")
        addButton(
            SimpleButton(
            { getInfoItem() },
            info.getSlots(),
            {}
        ))

        val bidMin = config.getItem("bid-min")
        val name = bidMin.getItem()!!.getName()
        addButton(
            SimpleButton(
                {
                    ItemBuilder(bidMin.getItem()!!).name(
                        MessageService.format(
                            name,
                            mapOf("%min_step%" to lot.minStep.toString())
                        )
                    ).build()
                },
                bidMin.getSlots()
            ) { data ->
                if (lot.status != LotStatus.RUNNING) { data.player.closeInventory(); return@SimpleButton }
                val amount = if (lot.currentBid <= 0) lot.startPrice else lot.currentBid + lot.minStep
                if (!EconomyManager.instance.has(data.player, if (lot.leader == data.player.uniqueId) lot.minStep else amount)) {
                    MessageService.sendMessage(
                        data.player,
                        ConfigManager.instance.messageConfig.warningsConfig.notEnoughMoney
                    )
                    return@SimpleButton
                }
                val am = auctionManager
                am.bidProcessor.processBid(lot, data.player, amount)
                update()
            })

        val bidCustom = config.getItem("bid-custom")
        addButton(
            SimpleButton(
                { bidCustom.getItem()!! },
                bidCustom.getSlots()
            ) { data ->
                if (lot.status != LotStatus.RUNNING) { data.player.closeInventory(); return@SimpleButton }
                MessageService.sendMessage(
                    data.player,
                    ConfigManager.instance.messageConfig.scannerConfig.specifyNumber
                )
                data.player.closeInventory()
                scanner.waitFor(data.player, 20L * 30) { message ->
                    if (lot.status != LotStatus.RUNNING) { data.player.closeInventory(); return@waitFor }
                    val amount = message.toDoubleOrNull()
                    if (amount == null) {
                        MessageService.sendMessage(
                            data.player,
                            ConfigManager.instance.messageConfig.errorsConfig.wrongNumber
                        )
                        return@waitFor
                    }
                    val minNeed = if (lot.currentBid <= 0) lot.startPrice else lot.currentBid + lot.minStep
                    if (amount < (if (lot.leader == data.player.uniqueId) lot.minStep else minNeed)) {
                        val need = if (lot.leader == data.player.uniqueId) lot.minStep else minNeed
                        MessageService.sendMessage(
                            data.player, ConfigManager.instance.messageConfig.warningsConfig.notMinBid,
                            mapOf("%need%" to need.toString())
                        )
                        return@waitFor
                    }
                    if (!EconomyManager.instance.has(data.player, amount)) {
                        MessageService.sendMessage(
                            data.player,
                            ConfigManager.instance.messageConfig.warningsConfig.notEnoughMoney
                        )
                        return@waitFor
                    }

                    val am = auctionManager
                    am.bidProcessor.processBid(lot, data.player, if (lot.leader == data.player.uniqueId) amount + lot.currentBid else amount)
                    update()
                }
            })

        val quickBid = config.getItem("quick-bid")
        val bids = quickBid.section.getIntegerList("bids")
        addButton(
            SimpleButton(
                { slot ->
                    val bid = bids[quickBid.getSlots().indexOf(slot)]
                    if (bid == null || lot.minStep > bid) return@SimpleButton ItemStack(Material.AIR)
                    val name = MessageService.format(quickBid.getItem()!!.getName(), mapOf("%bid%" to bid.toString()))
                    val lore = MessageService.format(quickBid.getLore(), mapOf("%bid%" to bid.toString()))
                    ItemBuilder(quickBid.getItem()!!).name(name).lore(lore).build()
                },
                quickBid.getSlots()
            ) { data ->
                if (lot.status != LotStatus.RUNNING) { data.player.closeInventory(); return@SimpleButton }
                val bid = bids[quickBid.getSlots().indexOf(data.slot)]
                if (bid == null || lot.minStep > bid) return@SimpleButton
                val amount = if (lot.currentBid <= 0) lot.startPrice + bid else lot.currentBid + bid
                if (!EconomyManager.instance.has(data.player, if (lot.leader == data.player.uniqueId) bid.toDouble() else amount)) {
                    MessageService.sendMessage(
                        data.player,
                        ConfigManager.instance.messageConfig.warningsConfig.notEnoughMoney
                    )
                    return@SimpleButton
                }
                val am = auctionManager
                am.bidProcessor.processBid(lot, data.player, amount)
                update()
            })
    }

    private fun getInfoItem(): ItemStack {
        val leader = if (lot.leader == null) "Нет ставок" else PlayerUtil.resolvePlayerName(lot.leader!!)
        val replacements = mapOf(
            "%current_bid%" to if (lot.currentBid > 0) lot.currentBid.toString() else "Нет ставок",
            "%start_price%" to lot.startPrice.toString(),
            "%min_step%" to lot.minStep.toString(),
            "%leader%" to leader,
            "%time_left%" to lot.endTime.diffFormat(TimeUtil.now(), ConfigManager.instance.config.auction.general.timeFormat)
        )
        val item = config.getItem("info")
        val builder = ItemBuilder(item.getItem()!!.clone())
        val lore = MessageService.format(item.getLore(), replacements)
        return builder.lore(lore).build()
    }
}