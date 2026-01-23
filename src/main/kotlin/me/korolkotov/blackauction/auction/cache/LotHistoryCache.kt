package me.korolkotov.blackauction.auction.cache

import me.korolkotov.blackauction.auction.model.LotHistory
import org.bukkit.OfflinePlayer
import java.util.concurrent.ConcurrentLinkedQueue

class LotHistoryCache {
    private val cache = ConcurrentLinkedQueue<LotHistory>()

    fun addAll(list: List<LotHistory>) {
        cache.addAll(list)
    }

    fun add(lotHistory: LotHistory) {
        cache.add(lotHistory)
    }

    fun getHistory() = cache.toList()

    fun getHistory(player: OfflinePlayer?) = if (player == null) getHistory() else cache.toList().filter { it.winnerUniqueId == player.uniqueId }
}