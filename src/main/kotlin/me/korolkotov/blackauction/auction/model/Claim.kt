package me.korolkotov.blackauction.auction.model

import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID

data class Claim(
    var id: Int,
    val playerUniqueId: UUID,
    val lotId: Int,
    val item: ItemStack,
    val pricePaid: Double,
    val wonAt: Instant,
    val addedAt: Instant
)