package me.korolkotov.blackauction.database.dao

import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.auction.model.LotStatus
import java.time.Instant
import java.util.UUID

interface LotDao {
    fun create(lot: Lot): Int
    fun findById(id: Int): Lot?
    fun updateStatus(id: Int, status: LotStatus)
    fun updateBid(id: Int, bid: Double, leader: UUID?)
    fun findActiveOrPlanned(now: Instant): List<Lot>
}