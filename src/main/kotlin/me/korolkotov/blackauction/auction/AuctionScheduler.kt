package me.korolkotov.blackauction.auction

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.korolkotov.blackauction.auction.model.*
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.coroutine.PluginCoroutineScope
import me.korolkotov.blackauction.economy.CommissionCalculator
import me.korolkotov.blackauction.economy.EconomyManager
import me.korolkotov.blackauction.logger.Logger
import me.korolkotov.blackauction.menu.impls.AdminLotMenu
import me.korolkotov.blackauction.menu.impls.LotMenu
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.PlayerUtil
import me.korolkotov.blackauction.util.TaskService
import me.korolkotov.blackauction.util.TimeUtil
import me.korolkotov.blackauction.util.getName
import org.bukkit.Bukkit
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class AuctionScheduler(
    private val manager: AuctionManager
) {
    private val notified = mutableMapOf<Lot, MutableList<Int>>()

    fun startScheduler() {
        PluginCoroutineScope.scope.launch {
            while (true) {
                val now = TimeUtil.now()
                manager.auctionCache.getLots().forEach { lot ->
                    when (lot.status) {
                        LotStatus.PLANNED -> {
                            val warnBeforeStart = ConfigManager.instance.config.auction.notifications.warnBeforeStart
                            val isStarted = lot.startTime.isBefore(now)
                            if (isStarted) {
                                if (lot.item.type.isEmpty) {
                                    cancel(lot)
                                    return@forEach
                                }
                                notified.remove(lot)
                                run(lot)
                                Logger.instance.debug("Lot ${lot.id} (slot ${lot.slot}) has just started")
                            } else {
                                val remaining = abs(ChronoUnit.SECONDS.between(now, lot.startTime).toInt())
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
                                Logger.instance.debug("Lot ${lot.id} (slot: ${lot.slot}) has just ended")
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
        PluginCoroutineScope.scope.launch {
            manager.repository.lotDao.update(lot)
        }
    }

    fun end(lot: Lot) {
        lot.changeStatus(LotStatus.FINISHED)
        manager.auctionCache.remove(lot.slot)
        closeInventories(lot)
        val finalPrice: Double?
        var commissionTaken = 0.0
        val now = TimeUtil.now()
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
                EconomyManager.instance.deposit(lot.economy, player, result.totalCommission)
            }

            val claim = Claim(
                0,
                winner,
                lot.id,
                lot.item.clone(),
                finalPrice,
                now, now
            )
            PluginCoroutineScope.scope.launch {
                val id = manager.repository.claimDao.add(claim)
                claim.id = id
                manager.claimsCache.addClaim(winner, claim)
            }
            val playerHistory = PlayerHistory(
                0,
                winner,
                lot.id,
                lot.item.getName(),
                lot.economy,
                finalPrice,
                now,
                null
            )
            PluginCoroutineScope.scope.launch {
                val id = manager.repository.playerHistoryDao.add(playerHistory)
                playerHistory.id = id
                manager.playerHistoryCache.addEntry(winner, playerHistory)
            }
            val player = Bukkit.getPlayer(winner)
            if (player != null) MessageService.sendMessage(player, ConfigManager.instance.messageConfig.notifications.youWon,
                mapOf("%item%" to lot.item.getName(), "%price%" to EconomyManager.instance.format(lot.economy, finalPrice)))
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
            PluginCoroutineScope.scope.launch {
                val id = manager.repository.claimDao.add(claim)
                claim.id = id
                manager.claimsCache.addClaim(admin, claim)
            }
            finalPrice = null
        }

        val winnerName = if (lot.leader == null) null else PlayerUtil.resolvePlayerName(lot.leader!!)
        val history = LotHistory(
            0,
            lot.id,
            lot.item.clone(),
            lot.leader,
            winnerName,
            lot.economy,
            finalPrice,
            commissionTaken,
            lot.startTime,
            lot.endTime,
            now
        )
        PluginCoroutineScope.scope.launch {
            val id = manager.repository.lotHistoryDao.add(history)
            history.id = id
            manager.lotHistoryCache.add(history)
        }
        Logger.instance.debug("Lot ${lot.id} has been finished.")
    }

    fun cancel(lot: Lot) {
        lot.changeStatus(LotStatus.CANCELLED)
        manager.auctionCache.remove(lot.slot)
        closeInventories(lot)
        if (lot.leader != null) {
            val leader = Bukkit.getOfflinePlayer(lot.leader!!)
            EconomyManager.instance.deposit(lot.economy, leader, lot.currentBid)
        }
        if (!lot.item.type.isEmpty) {
            val now = TimeUtil.now()
            val admin = lot.createdBy
            val claim = Claim(
                0,
                admin,
                lot.id,
                lot.item.clone(),
                0.0,
                now, now
            )
            PluginCoroutineScope.scope.launch {
                val id = manager.repository.claimDao.add(claim)
                claim.id = id
                manager.claimsCache.addClaim(admin, claim)
            }
        }
        Logger.instance.debug("Lot ${lot.id} has been cancelled.")
    }

    private fun closeInventories(lot: Lot) {
        Bukkit.getOnlinePlayers().forEach { player ->
            val openInv = player.openInventory.topInventory.holder ?: return@forEach
            when (openInv) {
                is LotMenu -> {
                    if (openInv.lot.id == lot.id)
                        TaskService.run { player.closeInventory() }
                }
                is AdminLotMenu -> {
                    if (openInv.lot.id == lot.id)
                        TaskService.run { player.closeInventory() }
                }
            }
        }
    }

    private fun Lot.changeStatus(new: LotStatus) {
        this.status = new
        PluginCoroutineScope.scope.launch {
            manager.repository.lotDao.updateStatus(this@changeStatus.id, new)
            Logger.instance.debug("Updated lot status (id $id, slot $slot) to ${new.name}")
        }
    }
}