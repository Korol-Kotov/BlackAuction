package me.korolkotov.blackauction.auction

import me.korolkotov.blackauction.load.LoadManagerInterface

class AuctionManager : LoadManagerInterface<AuctionManager> {
    private val cache = AuctionCache()

    override fun getInstance() = this

    override fun initialize() {
        TODO("Not yet implemented")
    }
}