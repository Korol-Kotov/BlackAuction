package me.korolkotov.blackauction.database.dao

import me.korolkotov.blackauction.auction.model.PlayerHistory
import java.time.Instant
import java.util.UUID

interface PlayerHistoryDao {
    fun add(entry: PlayerHistory): Int
    fun markClaimed(lotId: Int, claimedAt: Instant)
    fun findByPlayer(playerUuid: UUID): List<PlayerHistory>
}