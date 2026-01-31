package me.korolkotov.blackauction.database.dao.jdbc

import me.korolkotov.blackauction.auction.model.Claim
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.database.dao.ClaimDao
import me.korolkotov.blackauction.util.ItemSerializer
import me.korolkotov.blackauction.util.getInstant
import me.korolkotov.blackauction.util.setInstant
import java.sql.Statement
import java.util.UUID
import javax.sql.DataSource

class JdbcClaimDao(
    private val ds: DataSource
) : ClaimDao {
    override fun add(claim: Claim): Int =
        ds.connection.use { con ->
            val type = ConfigManager.instance.databaseConfig.type
            val sql = if (type.equals("sqlite", true)) {
                """
                INSERT INTO ba_claims (player_uuid, lot_id, item_data, price_paid, won_at, added_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_uuid, lot_id) DO UPDATE SET
                    item_data = excluded.item_data,
                    price_paid = excluded.price_paid,
                    won_at = excluded.won_at,
                    added_at = excluded.added_at
                """
            } else {
                """
                INSERT INTO ba_claims (player_uuid, lot_id, item_data, price_paid, won_at, added_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    item_data = VALUES(item_data),
                    price_paid = VALUES(price_paid),
                    won_at = VALUES(won_at),
                    added_at = VALUES(added_at)
                """
            }
            con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                var index = 1
                ps.setString(index++, claim.playerUniqueId.toString())
                ps.setInt(index++, claim.lotId)
                ps.setString(index++, ItemSerializer.serialize(claim.item))
                ps.setDouble(index++, claim.pricePaid)
                ps.setInstant(index++, claim.wonAt)
                ps.setInstant(index, claim.addedAt)
                ps.executeUpdate()

                ps.generatedKeys.use {
                    it.next()
                    it.getInt(1)
                }
            }
        }

    override fun findByPlayer(playerUuid: UUID): List<Claim> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM ba_claims WHERE player_uuid = ?"
            ).use { ps ->
                ps.setString(1, playerUuid.toString())
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Claim(
                                    rs.getInt("id"),
                                    playerUuid,
                                    rs.getInt("lot_id"),
                                    ItemSerializer.deserialize(rs.getString("item_data")),
                                    rs.getDouble("price_paid"),
                                    rs.getInstant("won_at"),
                                    rs.getInstant("added_at")
                                )
                            )
                        }
                    }
                }
            }
        }

    override fun findByLot(lotId: Int): List<Claim> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM ba_claims WHERE lot_id = ?"
            ).use { ps ->
                ps.setInt(1, lotId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Claim(
                                    rs.getInt("id"),
                                    UUID.fromString(rs.getString("player_uuid")),
                                    lotId,
                                    ItemSerializer.deserialize(rs.getString("item_data")),
                                    rs.getDouble("price_paid"),
                                    rs.getInstant("won_at"),
                                    rs.getInstant("added_at")
                                )
                            )
                        }
                    }
                }
            }
        }

    override fun delete(id: Int): Boolean =
        ds.connection.use { con ->
            con.prepareStatement(
                "DELETE FROM ba_claims WHERE id = ?"
            ).use { ps ->
                ps.setInt(1, id)
                ps.executeUpdate() == 1
            }
        }
}