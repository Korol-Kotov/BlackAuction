package me.korolkotov.blackauction.database.dao.jdbc

import me.korolkotov.blackauction.auction.model.Bid
import me.korolkotov.blackauction.database.dao.BidDao
import me.korolkotov.blackauction.util.getInstant
import me.korolkotov.blackauction.util.setInstant
import java.sql.Statement
import java.util.UUID
import javax.sql.DataSource

class JdbcBidDao(
    private val ds: DataSource
) : BidDao {
    override fun create(bid: Bid): Int =
        ds.connection.use { con ->
            val sql = """
                INSERT INTO ba_bids (lot_id, player_uuid, player_name, bid_amount, bid_time)
                VALUES (?, ?, ?, ?, ?)
            """
            con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                var index = 1
                ps.setInt(index++, bid.lotId)
                ps.setString(index++, bid.playerUniqueId.toString())
                ps.setString(index++, bid.playerName)
                ps.setDouble(index++, bid.amount)
                ps.setInstant(index, bid.bidTime)
                ps.executeUpdate()

                ps.generatedKeys.use {
                    it.next()
                    it.getInt(1)
                }
            }
        }

    override fun findByLot(lotId: Int): List<Bid> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM ba_bids WHERE lot_id = ? ORDER BY bid_time DESC"
            ).use { ps ->
                ps.setInt(1, lotId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Bid(
                                    rs.getInt("id"),
                                    rs.getInt("lot_id"),
                                    UUID.fromString(rs.getString("player_uuid")),
                                    rs.getString("player_name"),
                                    rs.getDouble("bid_amount"),
                                    rs.getInstant("bid_time")
                                )
                            )
                        }
                    }
                }
            }
        }

    override fun findByPlayer(playerUuid: UUID): List<Bid> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM ba_bids WHERE player_uuid = ? ORDER BY bid_time DESC"
            ).use { ps ->
                ps.setString(1, playerUuid.toString())
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Bid(
                                    rs.getInt("id"),
                                    rs.getInt("lot_id"),
                                    playerUuid,
                                    rs.getString("player_name"),
                                    rs.getDouble("bid_amount"),
                                    rs.getInstant("bid_time")
                                )
                            )
                        }
                    }
                }
            }
        }
}