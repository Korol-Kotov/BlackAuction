package me.korolkotov.blackauction.database.dao

import me.korolkotov.blackauction.auction.model.Bid
import java.util.UUID

interface BidDao {
    fun create(bid: Bid): Int
    fun findByLot(lotId: Int): List<Bid>
    fun findByPlayer(playerUuid: UUID): List<Bid>
}