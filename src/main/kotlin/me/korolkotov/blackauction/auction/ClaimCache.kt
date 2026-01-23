package me.korolkotov.blackauction.auction

import me.korolkotov.blackauction.auction.model.Claim
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ClaimCache {
    private val claims = ConcurrentHashMap<UUID, List<Claim>>()

    fun get(uniqueId: UUID): List<Claim>? = claims.getOrDefault(uniqueId, null)

    fun put(uniqueId: UUID, claims: List<Claim>) {
        this.claims[uniqueId] = claims.toList()
    }

    fun removeClaim(uniqueId: UUID, claim: Claim) {
        val list = get(uniqueId)?.toMutableList() ?: return
        list.removeAll { it.id == claim.id }
        put(uniqueId, list)
    }

    fun remove(uniqueId: UUID) {
        claims.remove(uniqueId)
    }

    fun has(uniqueId: UUID) = claims.containsKey(uniqueId)
}