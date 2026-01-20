package me.korolkotov.blackauction.database

import org.flywaydb.core.Flyway
import javax.sql.DataSource

class MigrationService(dataSource: DataSource) {
    private val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()

    fun migrate() {
        flyway.migrate()
    }
}