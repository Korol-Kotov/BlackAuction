package me.korolkotov.blackauction.auction.model

import me.korolkotov.blackauction.economy.EconomyType
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID

data class Lot(
    var id: Int,
    val slot: Int,
    var item: ItemStack,
    var economy: EconomyType,
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