package me.korolkotov.blackauction.auction.model

enum class LotStatus(
    val id: Int
) {
    PLANNED(0),
    RUNNING(1),
    FINISHED(2),
    CANCELLED(3)
}