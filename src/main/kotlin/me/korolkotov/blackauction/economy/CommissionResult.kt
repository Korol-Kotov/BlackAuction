package me.korolkotov.blackauction.economy

data class CommissionResult(
    val finalPrice: Double,
    val percentCommission: Double,
    val fixedCommission: Double,
    val totalCommission: Double,
    val sellerReceives: Double
)