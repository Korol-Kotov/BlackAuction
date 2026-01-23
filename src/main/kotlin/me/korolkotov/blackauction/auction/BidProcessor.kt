package me.korolkotov.blackauction.auction

import kotlinx.coroutines.launch
import me.korolkotov.blackauction.auction.model.Bid
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.coroutine.PluginCoroutineScope
import me.korolkotov.blackauction.economy.EconomyManager
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.getName
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Clock

class BidProcessor(
    private val manager: AuctionManager
) {
    fun processBid(lot: Lot, player: Player, amount: Double) {
        if (lot.currentBid >= amount) return
        if (lot.leader != null) {
            val player = Bukkit.getOfflinePlayer(lot.leader!!)
            EconomyManager.instance.deposit(player, lot.currentBid)
            if (player.isOnline) {
                MessageService.sendMessage(player.player!!, ConfigManager.instance.messageConfig.notifications.youWereOutbid,
                    mapOf("%item%" to lot.item.getName(), "%amount%" to amount.toString()))
            }
        }

        lot.leader = player.uniqueId
        lot.currentBid = amount
        EconomyManager.instance.withdraw(player, amount)
        val bid = Bid(
            0,
            lot.id,
            player.uniqueId,
            player.name,
            amount,
            Clock.systemUTC().instant()
        )
        PluginCoroutineScope.scope.launch {
            manager.repository.bidDao.create(bid)
            manager.repository.lotDao.updateBid(lot.id, amount, player.uniqueId)
        }
    }
}