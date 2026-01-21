package me.korolkotov.blackauction.database.dao

import me.korolkotov.blackauction.auction.model.Claim
import java.util.UUID

interface ClaimDao {
    fun add(claim: Claim): Int
    fun findByPlayer(playerUuid: UUID): List<Claim>
    fun findByLot(lotId: Int): List<Claim>
    fun delete(id: Int): Boolean
}