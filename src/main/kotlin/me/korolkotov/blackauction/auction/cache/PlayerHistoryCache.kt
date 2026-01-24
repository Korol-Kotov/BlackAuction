package me.korolkotov.blackauction.auction.cache

import me.korolkotov.blackauction.auction.model.Claim
import me.korolkotov.blackauction.auction.model.PlayerHistory
import java.time.Clock
import java.util.*

class PlayerHistoryCache : Cache<UUID, List<PlayerHistory>>() {
    fun addEntry(uniqueId: UUID, history: PlayerHistory) {
        val list = get(uniqueId)?.toMutableList() ?: return
        if (list.any { it.id == history.id }) return
        list.add(history)
        put(uniqueId, list)
    }

    fun setClaimed(uniqueId: UUID, claim: Claim) {
        val list = get(uniqueId)?.toMutableList() ?: return
        val history = list.firstOrNull { it.lotId == claim.lotId } ?: return
        history.claimedAt = Clock.systemUTC().instant()
    }
}