package me.korolkotov.blackauction.menu.impls

import kotlinx.coroutines.launch
import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.auction.model.Claim
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.coroutine.PluginCoroutineScope
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.MenuButton
import me.korolkotov.blackauction.menu.button.SimpleButton
import me.korolkotov.blackauction.util.ItemBuilder
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.TimeUtil
import me.korolkotov.blackauction.util.format
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ClaimsMenu(
    private val player: Player,
    private val page: Int
) : Menu("claims-menu") {
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

        val claimItem = config.getItem("claim")
        addButton(SimpleButton(
            { slot ->
                val claims = auctionManager.getClaims(this.player)
                val index = claimItem.getSlots().indexOf(slot) + (page - 1) * claimItem.getSlots().size
                val claim = claims.getOrNull(index) ?: return@SimpleButton ItemStack(Material.AIR)
                getItem(claim)
            },
            claimItem.getSlots()
        ) { data ->
            val claims = auctionManager.getClaims(this.player)
            val index = claimItem.getSlots().indexOf(data.slot) + (page - 1) * claimItem.getSlots().size
            val claim = claims.getOrNull(index) ?: return@SimpleButton
            if (give(player, claim.item)) {
                removeClaim(claim)
                update()
            } else {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.inventoryIsFull)
            }
        })

        val previousPage = config.getItem("previous-page")
        addButton(SimpleButton(
            { if (page > 1) previousPage.getItem()!! else ItemStack(Material.AIR) },
            previousPage.getSlots()
        ) { data ->
            if (inv.getItem(data.slot)?.type?.isEmpty != false) return@SimpleButton

            val menu = ClaimsMenu(player, page - 1)
            menu.open(data.player)
        })

        val nextPage = config.getItem("next-page")
        addButton(SimpleButton(
            {
                val claimItem = config.getItem("claim")
                val claims = auctionManager.getClaims(this.player)
                if (claims.size > claimItem.getSlots().size * page) nextPage.getItem()!!
                else ItemStack(Material.AIR)
            },
            nextPage.getSlots()
        ) { data ->
            if (inv.getItem(data.slot)?.type?.isEmpty != false) return@SimpleButton

            val menu = ClaimsMenu(player, page + 1)
            menu.open(data.player)
        })

        val claimAll = config.getItem("claim-all")
        addButton(SimpleButton(
            { claimAll.getItem()!! },
            claimAll.getSlots()
        ) { data ->
            val claims = auctionManager.getClaims(this.player)
            if (claims.isEmpty()) {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.claimsIsEmpty)
                return@SimpleButton
            }
            var given = 0
            for (claim in claims) {
                if (give(player, claim.item)) {
                    removeClaim(claim)
                    given++
                }
            }
            if (given <= 0) {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.inventoryIsFull)
            } else {
                update()
                if (given == claims.size) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.successConfig.claimedAll)
                } else {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.successConfig.claimedSome)
                }
            }
        })
    }

    private fun removeClaim(claim: Claim) {
        val am = auctionManager
        am.claimsCache.removeClaim(player.uniqueId, claim)
        am.playerHistoryCache.setClaimed(player.uniqueId, claim)
        PluginCoroutineScope.scope.launch {
            am.repository.claimDao.delete(claim.id)
            am.repository.playerHistoryDao.markClaimed(claim.lotId, TimeUtil.now())
        }
    }

    private fun give(player: Player, itemStack: ItemStack): Boolean {
        if (player.inventory.firstEmpty() != -1) {
            player.inventory.addItem(itemStack.clone())
            return true
        } else {
            val firstSimilar = player.inventory.firstOrNull { it.isSimilar(itemStack) && it.amount < it.maxStackSize } ?: return false
            firstSimilar.amount++
            return true
        }
    }

    private fun getItem(claim: Claim): ItemStack {
        val claimItem = config.getItem("claim")
        val item = claim.item.clone()
        val builder = ItemBuilder(item)
        val replacements = mapOf(
            "%date%" to claim.wonAt.format(ConfigManager.instance.config.auction.general.dateFormat),
            "%final_price%" to claim.pricePaid.format()
        )
        val lore = MessageService.format(claimItem.getLore(), replacements)
        return builder.lore(lore).build()
    }
}