package me.korolkotov.blackauction.auction

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.korolkotov.blackauction.auction.model.Claim
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.auction.model.LotHistory
import me.korolkotov.blackauction.auction.model.LotStatus
import me.korolkotov.blackauction.auction.model.PlayerHistory
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.coroutine.PluginCoroutineScope
import me.korolkotov.blackauction.economy.CommissionCalculator
import me.korolkotov.blackauction.economy.EconomyManager
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.PlayerUtil
import me.korolkotov.blackauction.util.getName
import org.bukkit.Bukkit
import java.time.Clock
import java.time.temporal.ChronoUnit

class AuctionScheduler(
    private val manager: AuctionManager
) {
    private val notified = mutableMapOf<Lot, MutableList<Int>>()

    fun startScheduler() {
        PluginCoroutineScope.scope.launch {
            while (true) {
                val now = Clock.systemUTC().instant()
                manager.auctionCache.getLots().forEach { lot ->
                    when (lot.status) {
                        LotStatus.PLANNED -> {
                            val warnBeforeStart = ConfigManager.instance.config.auction.notifications.warnBeforeStart
                            val isStarted = lot.startTime.isBefore(now)
                            if (isStarted) {
                                notified.remove(lot)
                                run(lot)
                            } else {
                                val remaining = ChronoUnit.SECONDS.between(now, lot.startTime).toInt()
                                val list = notified.getOrDefault(lot, mutableListOf())
                                if (remaining in warnBeforeStart && remaining !in list) {
                                    list.add(remaining)
                                    notified[lot] = list
                                    MessageService.broadcast(ConfigManager.instance.messageConfig.notifications.auctionStarting,
                                        mapOf("%time%" to remaining.toString()))
                                }
                            }
                        }
                        LotStatus.RUNNING -> {
                            val warnLeaderBeforeEnd = ConfigManager.instance.config.auction.notifications.warnLeaderBeforeEnd
                            val isEnded = lot.endTime.isBefore(now)
                            if (isEnded) {
                                notified.remove(lot)
                                end(lot)
                            } else {
                                if (lot.leader == null) return@forEach
                                val leader = Bukkit.getPlayer(lot.leader!!) ?: return@forEach
                                val remaining = ChronoUnit.SECONDS.between(now, lot.endTime).toInt()
                                val list = notified.getOrDefault(lot, mutableListOf())
                                if (remaining in warnLeaderBeforeEnd && remaining !in list) {
                                    list.add(remaining)
                                    notified[lot] = list
                                    MessageService.sendMessage(leader, ConfigManager.instance.messageConfig.notifications.auctionEningSoon,
                                        mapOf("%time%" to remaining.toString()))
                                }
                            }
                        }
                        LotStatus.FINISHED,
                        LotStatus.CANCELLED -> {
                            manager.auctionCache.remove(lot.slot)
                            return@forEach
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    fun run(lot: Lot) {
        lot.changeStatus(LotStatus.RUNNING)
        MessageService.broadcast(ConfigManager.instance.messageConfig.notifications.auctionStarted)
    }

    fun end(lot: Lot) {
        lot.changeStatus(LotStatus.FINISHED)
        manager.auctionCache.remove(lot.slot)
        val finalPrice: Double?
        var commissionTaken = 0.0
        val now = Clock.systemUTC().instant()
        if (lot.leader != null) {
            val winner = lot.leader!!
            val percent = ConfigManager.instance.config.auction.economy.commissionPercent
            val fixed = ConfigManager.instance.config.auction.economy.fixedCommission
            val commission = CommissionCalculator(percent, fixed)
            val result = commission.calculate(lot.currentBid)
            finalPrice = result.finalPrice
            commissionTaken = result.totalCommission

            if (ConfigManager.instance.config.auction.economy.commissionDestination.equals("TREASURY", true)) {
                val account = ConfigManager.instance.config.auction.economy.treasuryAccount
                val player = Bukkit.getOfflinePlayer(account)
                EconomyManager.instance.deposit(player, result.totalCommission)
            }

            val claim = Claim(
                0,
                winner,
                lot.id,
                lot.item.clone(),
                finalPrice,
                now, now
            )
            PluginCoroutineScope.scope.launch { manager.repository.claimDao.add(claim) }
            val playerHistory = PlayerHistory(
                0,
                winner,
                lot.id,
                lot.item.getName(),
                finalPrice,
                now,
                null
            )
            PluginCoroutineScope.scope.launch { manager.repository.playerHistoryDao.add(playerHistory) }
            val player = Bukkit.getPlayer(winner)
            if (player != null) MessageService.sendMessage(player, ConfigManager.instance.messageConfig.notifications.youWon,
                mapOf("%item%" to lot.item.getName(), "%price%" to finalPrice.toString()))
        } else {
            val admin = lot.createdBy
            val claim = Claim(
                0,
                admin,
                lot.id,
                lot.item.clone(),
                0.0,
                now, now
            )
            PluginCoroutineScope.scope.launch { manager.repository.claimDao.add(claim) }
            finalPrice = null
        }

        val winnerName = if (lot.leader == null) null else PlayerUtil.resolvePlayerName(lot.leader!!)
        val history = LotHistory(
            0,
            lot.id,
            lot.item.clone(),
            lot.leader,
            winnerName,
            finalPrice,
            commissionTaken,
            lot.startTime,
            lot.endTime,
            now
        )
        manager.lotHistoryCache.add(history)
        PluginCoroutineScope.scope.launch { manager.repository.lotHistoryDao.add(history) }
    }

    fun cancel(lot: Lot) {
        lot.changeStatus(LotStatus.CANCELLED)
        manager.auctionCache.remove(lot.slot)
        if (lot.leader != null) {
            val leader = Bukkit.getOfflinePlayer(lot.leader!!)
            EconomyManager.instance.deposit(leader, lot.currentBid)
        }
        val now = Clock.systemUTC().instant()
        val admin = lot.createdBy
        val claim = Claim(
            0,
            admin,
            lot.id,
            lot.item.clone(),
            0.0,
            now, now
        )
        PluginCoroutineScope.scope.launch { manager.repository.claimDao.add(claim) }
    }

    private fun Lot.changeStatus(new: LotStatus) {
        this.status = new
        PluginCoroutineScope.scope.launch { manager.repository.lotDao.updateStatus(this@changeStatus.id, new) }
    }
}