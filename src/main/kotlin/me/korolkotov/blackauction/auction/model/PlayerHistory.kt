package me.korolkotov.blackauction.auction.model

import me.korolkotov.blackauction.economy.EconomyType
import java.time.Instant
import java.util.*

data class PlayerHistory(
    var id: Int,
    val playerUniqueId: UUID,
    val lotId: Int,
    val item: String,
    val economy: EconomyType,
    val finalPrice: Double,
    val wonAt: Instant,
    var claimedAt: Instant?
)