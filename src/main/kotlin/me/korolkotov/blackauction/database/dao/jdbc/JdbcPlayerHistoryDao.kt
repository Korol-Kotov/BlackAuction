package me.korolkotov.blackauction.database.dao.jdbc

import me.korolkotov.blackauction.auction.model.PlayerHistory
import me.korolkotov.blackauction.database.dao.PlayerHistoryDao
import me.korolkotov.blackauction.economy.EconomyType
import me.korolkotov.blackauction.util.getInstant
import me.korolkotov.blackauction.util.setInstant
import java.sql.Statement
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

class JdbcPlayerHistoryDao(
    private val ds: DataSource
) : PlayerHistoryDao {
    override fun add(entry: PlayerHistory): Int =
        ds.connection.use { con ->
            val sql = """
                INSERT INTO ba_player_history
                (player_uuid, lot_id, item_name, economy, final_price, won_at, claimed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """
            con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                var index = 1
                ps.setString(index++, entry.playerUniqueId.toString())
                ps.setInt(index++, entry.lotId)
                ps.setString(index++, entry.item.take(255))
                ps.setString(index++, entry.economy.name)
                ps.setDouble(index++, entry.finalPrice)
                ps.setInstant(index++, entry.wonAt)
                ps.setTimestamp(index, entry.claimedAt?.let { Timestamp.from(it) })

                ps.executeUpdate()
                ps.generatedKeys.use {
                    it.next()
                    it.getInt(1)
                }
            }
        }

    override fun markClaimed(lotId: Int, claimedAt: Instant) {
        ds.connection.use { con ->
            con.prepareStatement(
                "UPDATE ba_player_history SET claimed_at = ? WHERE lot_id = ?"
            ).use { ps ->
                ps.setInstant(1, claimedAt)
                ps.setInt(2, lotId)
                ps.executeUpdate()
            }
        }
    }

    override fun findByPlayer(playerUuid: UUID): List<PlayerHistory> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM ba_player_history WHERE player_uuid = ? ORDER BY won_at DESC"
            ).use { ps ->
                ps.setString(1, playerUuid.toString())
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                PlayerHistory(
                                    rs.getInt("id"),
                                    playerUuid,
                                    rs.getInt("lot_id"),
                                    rs.getString("item_name"),
                                    EconomyType.entries.first { it.name.equals(rs.getString("economy"), true) },
                                    rs.getDouble("final_price"),
                                    rs.getInstant("won_at"),
                                    rs.getTimestamp("claimed_at")?.toInstant()
                                )
                            )
                        }
                    }
                }
            }
        }
}