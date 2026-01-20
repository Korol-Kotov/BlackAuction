package me.korolkotov.blackauction.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.load.LoadManagerInterface

class DatabaseManager : LoadManagerInterface<DatabaseManager> {
    lateinit var dataSource: HikariDataSource

    override fun getInstance() = this

    override fun initialize() {
        val config = ConfigManager.instance.databaseConfig

        val hikari = HikariConfig().apply {
            jdbcUrl =
                "jdbc:mysql://${config.mysql.host}:${config.mysql.port}/${config.mysql.database}" +
                        "?useSSL=${config.mysql.connectionProperties.useSSL}" +
                        "&autoReconnect=${config.mysql.connectionProperties.autoReconnect}" +
                        "&characterEncoding=${config.mysql.connectionProperties.characterEncoding}" +
                        "&cachePrepStmts=${config.mysql.connectionProperties.cachePrepStmts}" +
                        "&prepStmtCacheSize=${config.mysql.connectionProperties.prepStmtCacheSize}" +
                        "&prepStmtCacheSqlLimit=${config.mysql.connectionProperties.prepStmtCacheSqlLimit}"

            username = config.mysql.user
            password = config.mysql.password

            maximumPoolSize = config.mysql.pool.poolSize
            minimumIdle = config.mysql.pool.minimumIdle

            idleTimeout = config.mysql.pool.idleTimeout
            connectionTimeout = config.mysql.pool.connectionTimeout
            maxLifetime = config.mysql.pool.maxLifetime

            driverClassName = "com.mysql.cj.jdbc.Driver"
        }

        dataSource = HikariDataSource(hikari)

        MigrationService(dataSource).migrate()
    }

    override fun terminate() {
        dataSource.close()
    }
}