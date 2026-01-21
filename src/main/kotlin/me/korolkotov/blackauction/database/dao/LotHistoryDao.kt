package me.korolkotov.blackauction.database.dao

import me.korolkotov.blackauction.auction.model.LotHistory
import java.util.*

interface LotHistoryDao {
    fun add(history: LotHistory): Int
    fun findByWinner(uuid: UUID): List<LotHistory>
    fun findRecent(limit: Int = 50): List<LotHistory>
}