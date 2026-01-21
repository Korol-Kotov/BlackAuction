package me.korolkotov.blackauction.database.repository

import me.korolkotov.blackauction.database.dao.BidDao
import me.korolkotov.blackauction.database.dao.ClaimDao
import me.korolkotov.blackauction.database.dao.LotDao
import me.korolkotov.blackauction.database.dao.LotHistoryDao
import me.korolkotov.blackauction.database.dao.PlayerHistoryDao

data class AuctionRepository(
    val bidDao: BidDao,
    val claimDao: ClaimDao,
    val lotDao: LotDao,
    val lotHistoryDao: LotHistoryDao,
    val playerHistoryDao: PlayerHistoryDao
)