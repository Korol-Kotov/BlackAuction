package me.korolkotov.blackauction.auction

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.auction.model.LotStatus
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.coroutine.PluginCoroutineScope
import me.korolkotov.blackauction.util.MessageService
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
    }

    fun cancel(lot: Lot) {
        lot.changeStatus(LotStatus.CANCELLED)
    }

    private fun Lot.changeStatus(new: LotStatus) {
        this.status = new
        PluginCoroutineScope.scope.launch { manager.repository.lotDao.updateStatus(this@changeStatus.id, new) }
    }
}