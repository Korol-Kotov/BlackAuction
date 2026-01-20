package me.korolkotov.blackauction.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.load.LoadManagerInterface
import me.korolkotov.blackauction.logger.Logger

class DatabaseManager : LoadManagerInterface<DatabaseManager> {
    lateinit var dataSource: HikariDataSource

    override fun getInstance() = this

    override fun initialize() {
        val mysql = ConfigManager.instance.databaseConfig.mysql
        val hikari = HikariConfig().apply {
            jdbcUrl =
                "jdbc:mysql://${mysql.host}:${mysql.port}/${mysql.database}" +
                        "?useSSL=${mysql.connectionProperties.useSSL}" +
                        "&autoReconnect=${mysql.connectionProperties.autoReconnect}" +
                        "&characterEncoding=${mysql.connectionProperties.characterEncoding}" +
                        "&cachePrepStmts=${mysql.connectionProperties.cachePrepStmts}" +
                        "&prepStmtCacheSize=${mysql.connectionProperties.prepStmtCacheSize}" +
                        "&prepStmtCacheSqlLimit=${mysql.connectionProperties.prepStmtCacheSqlLimit}"

            username = mysql.user
            password = mysql.password

            maximumPoolSize = mysql.pool.poolSize
            minimumIdle = mysql.pool.minimumIdle

            idleTimeout = mysql.pool.idleTimeout
            connectionTimeout = mysql.pool.connectionTimeout
            maxLifetime = mysql.pool.maxLifetime

            driverClassName = "com.mysql.cj.jdbc.Driver"
        }

        dataSource = HikariDataSource(hikari)
        Logger.instance.debug("Data source has been initialized.")

        MigrationService(dataSource).migrate()
        Logger.instance.debug("Database has been migrated.")
    }

    override fun terminate() {
        dataSource.close()
    }
}