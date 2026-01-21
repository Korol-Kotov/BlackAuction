package me.korolkotov.blackauction.auction

import me.korolkotov.blackauction.auction.model.Lot
import java.util.concurrent.ConcurrentHashMap

class AuctionCache {
    private val lots = ConcurrentHashMap<Int, Lot>()

    fun get(slot: Int): Lot? = lots[slot]
    fun getLots() = lots.values.toList()

    fun put(slot: Int, lot: Lot) {
        lots[slot] = lot
    }

    fun remove(slot: Int) {
        lots.remove(slot)
    }

    fun nextSlot() = lots.size
}