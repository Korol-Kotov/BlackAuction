package me.korolkotov.blackauction.auction.cache

import me.korolkotov.blackauction.auction.model.Claim
import java.util.*

class ClaimCache : Cache<UUID, List<Claim>>() {
    fun removeClaim(uniqueId: UUID, claim: Claim) {
        val list = get(uniqueId)?.toMutableList() ?: return
        list.removeAll { it.id == claim.id }
        put(uniqueId, list)
    }
}