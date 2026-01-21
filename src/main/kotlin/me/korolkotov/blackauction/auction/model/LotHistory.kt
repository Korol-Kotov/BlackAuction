package me.korolkotov.blackauction.auction.model

import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID

data class LotHistory(
    val id: Int,
    val lotId: Int,
    val item: ItemStack,
    val winnerUniqueId: UUID?,
    val winnerName: String?,
    val finalPrice: Double?,
    val commissionTaken: Double,
    val startTime: Instant,
    val endTime: Instant,
    val completedAt: Instant
)