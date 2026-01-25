package me.korolkotov.blackauction.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.database.dao.BidDao
import me.korolkotov.blackauction.database.dao.ClaimDao
import me.korolkotov.blackauction.database.dao.LotDao
import me.korolkotov.blackauction.database.dao.LotHistoryDao
import me.korolkotov.blackauction.database.dao.PlayerHistoryDao
import me.korolkotov.blackauction.database.dao.jdbc.JdbcBidDao
import me.korolkotov.blackauction.database.dao.jdbc.JdbcClaimDao
import me.korolkotov.blackauction.database.dao.jdbc.JdbcLotDao
import me.korolkotov.blackauction.database.dao.jdbc.JdbcLotHistoryDao
import me.korolkotov.blackauction.database.dao.jdbc.JdbcPlayerHistoryDao
import me.korolkotov.blackauction.database.repository.AuctionRepository
import me.korolkotov.blackauction.load.LoadManagerInterface
import me.korolkotov.blackauction.logger.Logger

class DatabaseManager : LoadManagerInterface<DatabaseManager> {
    lateinit var dataSource: HikariDataSource

    lateinit var bidDao: BidDao
    lateinit var claimDao: ClaimDao
    lateinit var lotDao: LotDao
    lateinit var lotHistoryDao: LotHistoryDao
    lateinit var playerHistoryDao: PlayerHistoryDao

    lateinit var auctionRepository: AuctionRepository

    override fun getInstance() = this

    override fun initialize() {
        val hikari = if (ConfigManager.instance.databaseConfig.type.equals("mysql", true)) {
            val mysql = ConfigManager.instance.databaseConfig.mysql
            HikariConfig().apply {
                jdbcUrl =
                    "jdbc:mysql://${mysql.host}:${mysql.port}/${mysql.database}" +
                            "?useSSL=${mysql.connectionProperties.useSSL}" +
                            "&autoReconnect=${mysql.connectionProperties.autoReconnect}" +
                            "&characterEncoding=${mysql.connectionProperties.characterEncoding}" +
                            "&cachePrepStmts=${mysql.connectionProperties.cachePrepStmts}" +
                            "&prepStmtCacheSize=${mysql.connectionProperties.prepStmtCacheSize}" +
                            "&prepStmtCacheSqlLimit=${mysql.connectionProperties.prepStmtCacheSqlLimit}" +
                            "&serverTimezone=UTC"

                username = mysql.user
                password = mysql.password

                maximumPoolSize = mysql.pool.poolSize
                minimumIdle = mysql.pool.minimumIdle

                idleTimeout = mysql.pool.idleTimeout
                connectionTimeout = mysql.pool.connectionTimeout
                maxLifetime = mysql.pool.maxLifetime

                driverClassName = "com.mysql.cj.jdbc.Driver"
            }
        } else {
            val sqlite = ConfigManager.instance.databaseConfig.sqlite
            HikariConfig().apply {
                jdbcUrl = "jdbc:sqlite:${sqlite.file}"

                maximumPoolSize = 1
                minimumIdle = 1
                idleTimeout = 0
                maxLifetime = 0
                connectionTimeout = 30_000

                isAutoCommit = true

                driverClassName = "org.sqlite.JDBC"
            }
        }

        dataSource = HikariDataSource(hikari)
        Logger.instance.debug("Data source has been initialized.")

        MigrationService(dataSource).migrate()
        Logger.instance.debug("Database has been migrated.")

        bidDao = JdbcBidDao(dataSource)
        claimDao = JdbcClaimDao(dataSource)
        lotDao = JdbcLotDao(dataSource)
        lotHistoryDao = JdbcLotHistoryDao(dataSource)
        playerHistoryDao = JdbcPlayerHistoryDao(dataSource)

        auctionRepository = AuctionRepository(bidDao, claimDao, lotDao, lotHistoryDao, playerHistoryDao)
    }

    override fun terminate() {
        dataSource.close()
    }
}