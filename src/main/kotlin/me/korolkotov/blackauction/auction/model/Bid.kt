package me.korolkotov.blackauction.auction.model

import java.time.Instant
import java.util.UUID

data class Bid(
    val id: Int,
    val lotId: Int,
    val playerUniqueId: UUID,
    val playerName: String,
    val amount: Double,
    val bidTime: Instant
)