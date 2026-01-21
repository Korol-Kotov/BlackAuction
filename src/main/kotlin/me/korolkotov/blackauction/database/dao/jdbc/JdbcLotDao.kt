package me.korolkotov.blackauction.database.dao.jdbc

import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.auction.model.LotStatus
import me.korolkotov.blackauction.database.dao.LotDao
import me.korolkotov.blackauction.util.ItemSerializer
import me.korolkotov.blackauction.util.getInstant
import me.korolkotov.blackauction.util.setInstant
import java.sql.Statement
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

class JdbcLotDao(
    private val ds: DataSource
) : LotDao {
    override fun create(lot: Lot): Int =
        ds.connection.use { con ->
            val sql = """
                INSERT INTO ba_lots
                (slot, item_data, start_price, min_bid_step, start_time, end_time, status, created_by, created_at, current_price, current_winner_uuid)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
            con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                var index = 1
                ps.setString(index++, ItemSerializer.serialize(lot.item))
                ps.setInt(index++, lot.slot)
                ps.setDouble(index++, lot.startPrice)
                ps.setDouble(index++, lot.minStep)
                ps.setInstant(index++, lot.startTime)
                ps.setInstant(index++, lot.endTime)
                ps.setInt(index++, lot.status.id)
                ps.setString(index++, lot.createdBy.toString())
                ps.setInstant(index++, lot.createdAt)
                ps.setDouble(index++, lot.currentBid)
                ps.setString(index, lot.leader?.toString())
                ps.executeUpdate()

                ps.generatedKeys.use {
                    it.next()
                    it.getInt(1)
                }
            }
        }

    override fun findById(id: Int): Lot? =
        ds.connection.use { con ->
            con.prepareStatement("SELECT * FROM ba_lots WHERE id = ?").use { ps ->
                ps.setInt(1, id)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return null

                    Lot(
                        rs.getInt("id"),
                        rs.getInt("slot"),
                        ItemSerializer.deserialize(rs.getString("item_data")),
                        rs.getDouble("start_price"),
                        rs.getDouble("min_bid_step"),
                        rs.getInstant("start_time"),
                        rs.getInstant("end_time"),
                        LotStatus.entries.first { it.id == rs.getInt("status") },
                        UUID.fromString(rs.getString("created_by")),
                        rs.getInstant("created_at"),
                        rs.getDouble("current_price"),
                        rs.getString("current_winner_uuid")?.let(UUID::fromString)
                    )
                }
            }
        }

    override fun updateStatus(id: Int, status: LotStatus) {
        ds.connection.use { con ->
            con.prepareStatement(
                "UPDATE ba_lots SET status = ? WHERE id = ?"
            ).use {
                it.setInt(1, status.id)
                it.setInt(2, id)
                it.executeUpdate()
            }
        }
    }

    override fun updateBid(id: Int, bid: Double, leader: UUID?) {
        ds.connection.use { con ->
            con.prepareStatement(
                "UPDATE ba_lots SET current_price = ?, current_winner_uuid = ? WHERE id = ?"
            ).use {
                it.setDouble(1, bid)
                it.setString(2, leader?.toString())
                it.setInt(3, id)
                it.executeUpdate()
            }
        }
    }

    override fun findActiveOrPlanned(now: Instant): List<Lot> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM ba_lots WHERE status IN (0, 1) AND start_time <= ? AND end_time > ?"
            ).use { ps ->
                ps.setInstant(1, now)
                ps.setInstant(2, now)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(findById(rs.getInt("id"))!!)
                        }
                    }
                }
            }
        }
}