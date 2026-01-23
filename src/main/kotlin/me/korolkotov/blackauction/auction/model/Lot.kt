package me.korolkotov.blackauction.auction.model

import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID

data class Lot(
    val id: Int,
    val slot: Int,
    var item: ItemStack,
    var startPrice: Double,
    var minStep: Double,
    var startTime: Instant,
    var endTime: Instant,
    var status: LotStatus,
    var createdBy: UUID,
    val createdAt: Instant,

    var currentBid: Double,
    var leader: UUID?
) {
    var extensions = 0
}