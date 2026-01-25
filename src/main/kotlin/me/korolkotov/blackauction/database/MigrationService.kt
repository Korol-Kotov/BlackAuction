package me.korolkotov.blackauction.database

import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.config.ConfigManager
import org.flywaydb.core.Flyway
import javax.sql.DataSource

class MigrationService(dataSource: DataSource) {
    private val flyway = Flyway.configure(Main.instance.javaClass.getClassLoader())
        .dataSource(dataSource)
        .baselineOnMigrate(true)
        .locations("classpath:db/migration/${ConfigManager.instance.databaseConfig.type}/")
        .mixed(true)
        .validateMigrationNaming(true)
        .load()

    fun migrate() {
        flyway.migrate()
    }
}