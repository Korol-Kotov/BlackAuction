package me.korolkotov.blackauction.auction

import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.auction.model.LotStatus
import java.util.concurrent.ConcurrentHashMap

class AuctionCache {
    private val lots = ConcurrentHashMap<Int, Lot>()

    fun get(slot: Int): Lot? = lots[slot]
    fun getActive(): Collection<Lot> =
        lots.values.filter { it.status == LotStatus.RUNNING }

    fun put(slot: Int, lot: Lot) {
        lots[slot] = lot
    }

    fun remove(slot: Int) {
        lots.remove(slot)
    }
}