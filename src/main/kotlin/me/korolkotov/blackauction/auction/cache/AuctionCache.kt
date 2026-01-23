package me.korolkotov.blackauction.auction.cache

import me.korolkotov.blackauction.auction.model.Lot

class AuctionCache : Cache<Int, Lot>() {
    fun getLots() = cache.values.toList()

    fun nextSlot() = cache.size
}