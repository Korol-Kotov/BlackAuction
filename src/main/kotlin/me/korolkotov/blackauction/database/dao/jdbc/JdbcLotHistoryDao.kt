package me.korolkotov.blackauction.database.dao.jdbc

import me.korolkotov.blackauction.auction.model.LotHistory
import me.korolkotov.blackauction.database.dao.LotHistoryDao
import me.korolkotov.blackauction.util.ItemSerializer
import me.korolkotov.blackauction.util.getInstant
import me.korolkotov.blackauction.util.getName
import me.korolkotov.blackauction.util.setInstant
import java.sql.Statement
import java.util.*
import javax.sql.DataSource

class JdbcLotHistoryDao(
    private val ds: DataSource
) : LotHistoryDao {
    override fun add(history: LotHistory) =
        ds.connection.use { con ->
            val sql = """
                INSERT INTO ba_history 
                (lot_id, item_name, item_data, winner_uuid, winner_name, final_price, commission_taken, start_time, end_time, completed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
            con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                ps.setInt(1, history.lotId)
                ps.setString(2, history.item.getName().take(255))
                ps.setString(3, ItemSerializer.serialize(history.item))
                ps.setString(4, history.winnerUniqueId?.toString())
                ps.setString(5, history.winnerName)
                ps.setDouble(6, history.finalPrice ?: 0.0)
                ps.setDouble(7, history.commissionTaken)
                ps.setInstant(8, history.startTime)
                ps.setInstant(9, history.endTime)
                ps.setInstant(10, history.completedAt)

                ps.executeUpdate()
                ps.generatedKeys.use {
                    it.next()
                    it.getInt(1)
                }
            }
        }

    override fun findByWinner(uuid: UUID): List<LotHistory> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM ba_history WHERE winner_uuid = ? ORDER BY completed_at DESC"
            ).use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                LotHistory(
                                    rs.getInt("id"),
                                    rs.getInt("lot_id"),
                                    ItemSerializer.deserialize(rs.getString("item_data")),
                                    rs.getString("winner_uuid")?.let(UUID::fromString),
                                    rs.getString("winner_name"),
                                    rs.getDouble("final_price").takeIf { !rs.wasNull() },
                                    rs.getDouble("commission_taken"),
                                    rs.getInstant("start_time"),
                                    rs.getInstant("end_time"),
                                    rs.getInstant("completed_at")
                                )
                            )
                        }
                    }
                }
            }
        }

    override fun findRecent(limit: Int): List<LotHistory> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM ba_history ORDER BY completed_at DESC LIMIT ?"
            ).use { ps ->
                ps.setInt(1, limit)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                LotHistory(
                                    rs.getInt("id"),
                                    rs.getInt("lot_id"),
                                    ItemSerializer.deserialize(rs.getString("item_data")),
                                    rs.getString("winner_uuid")?.let(UUID::fromString),
                                    rs.getString("winner_name"),
                                    rs.getDouble("final_price").takeIf { !rs.wasNull() },
                                    rs.getDouble("commission_taken"),
                                    rs.getInstant("start_time"),
                                    rs.getInstant("end_time"),
                                    rs.getInstant("completed_at")
                                )
                            )
                        }
                    }
                }
            }
        }
}