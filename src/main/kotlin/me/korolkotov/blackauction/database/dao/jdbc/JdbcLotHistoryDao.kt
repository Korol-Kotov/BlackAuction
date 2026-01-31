package me.korolkotov.blackauction.database.dao.jdbc

import me.korolkotov.blackauction.auction.model.LotHistory
import me.korolkotov.blackauction.database.dao.LotHistoryDao
import me.korolkotov.blackauction.economy.EconomyType
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
                (lot_id, item_name, item_data, winner_uuid, winner_name, economy, final_price, commission_taken, start_time, end_time, completed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
            con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                var index = 1
                ps.setInt(index++, history.lotId)
                ps.setString(index++, history.item.getName().take(255))
                ps.setString(index++, ItemSerializer.serialize(history.item))
                ps.setString(index++, history.winnerUniqueId?.toString())
                ps.setString(index++, history.winnerName)
                ps.setString(index++, history.economy.name)
                ps.setDouble(index++, history.finalPrice ?: 0.0)
                ps.setDouble(index++, history.commissionTaken)
                ps.setInstant(index++, history.startTime)
                ps.setInstant(index++, history.endTime)
                ps.setInstant(index, history.completedAt)

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
                                    EconomyType.entries.first { it.name.equals(rs.getString("economy"), true) },
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
                                    EconomyType.entries.first { it.name.equals(rs.getString("economy"), true) },
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