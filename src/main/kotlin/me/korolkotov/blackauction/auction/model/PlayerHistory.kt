package me.korolkotov.blackauction.auction.model

import java.time.Instant
import java.util.*

data class PlayerHistory(
    val id: Int,
    val playerUniqueId: UUID,
    val lotId: Int,
    val item: String,
    val finalPrice: Double,
    val wonAt: Instant,
    val claimedAt: Instant?
)