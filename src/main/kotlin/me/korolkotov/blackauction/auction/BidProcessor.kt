package me.korolkotov.blackauction.auction

import kotlinx.coroutines.launch
import me.korolkotov.blackauction.auction.model.Bid
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.coroutine.PluginCoroutineScope
import me.korolkotov.blackauction.economy.EconomyManager
import me.korolkotov.blackauction.logger.Logger
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.TimeUtil
import me.korolkotov.blackauction.util.getName
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.temporal.ChronoUnit

class BidProcessor(
    private val manager: AuctionManager
) {
    fun processBid(lot: Lot, player: Player, amount: Double) {
        if (lot.currentBid >= amount) return
        if (lot.leader != null) {
            val leader = Bukkit.getOfflinePlayer(lot.leader!!)
            EconomyManager.instance.deposit(leader, lot.currentBid)
            if (leader.isOnline && leader.uniqueId != player.uniqueId) {
                MessageService.sendMessage(leader.player!!, ConfigManager.instance.messageConfig.notifications.youWereOutbid,
                    mapOf("%item%" to lot.item.getName(), "%amount%" to amount.toString()))
            }
        }

        lot.leader = player.uniqueId
        lot.currentBid = amount
        EconomyManager.instance.withdraw(player, amount)
        Logger.instance.debug("New bid to ${lot.id}: ${player.name} made a bid of $amount")
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.successConfig.bidPlaced,
            mapOf("%amount%" to amount.toString()))
        val now = TimeUtil.now()
        val bid = Bid(
            0,
            lot.id,
            player.uniqueId,
            player.name,
            amount,
            now
        )
        PluginCoroutineScope.scope.launch {
            manager.repository.bidDao.create(bid)
            manager.repository.lotDao.updateBid(lot.id, amount, player.uniqueId)
        }

        if (!ConfigManager.instance.config.auction.antiSnipe.enabled) return
        if (lot.extensions >= ConfigManager.instance.config.auction.antiSnipe.maxExtensions) return
        if (ChronoUnit.SECONDS.between(now, lot.endTime)
            <= ConfigManager.instance.config.auction.antiSnipe.triggerSeconds) {
            lot.extensions++
            val extend = ConfigManager.instance.config.auction.antiSnipe.extendBySeconds
            lot.endTime = lot.endTime.plusSeconds(extend.toLong())
            MessageService.broadcast(ConfigManager.instance.messageConfig.notifications.auctionExtended,
                mapOf("%seconds%" to extend.toString()))
        }
    }
}