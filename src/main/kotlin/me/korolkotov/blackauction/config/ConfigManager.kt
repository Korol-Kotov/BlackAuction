package me.korolkotov.blackauction.config

import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.load.LoadManagerInterface
import me.korolkotov.blackauction.util.MessageService
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager : LoadManagerInterface<ConfigManager> {
    companion object {
        lateinit var instance: ConfigManager private set
    }

    private val dataFolder get() = Main.instance.dataFolder

    lateinit var databaseConfig: DatabaseConfig

    init {
        instance = this
    }

    override fun getInstance() = this

    override fun initialize() {
        val database = loadOrCreate("database.yml")
        databaseConfig = DatabaseConfig(database)
    }

    override fun reload() {
        initialize()
    }

    private fun loadOrCreate(fileName: String, fill: Boolean = true): YamlConfiguration {
        val file = File(dataFolder, fileName)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            if (fill) {
                this::class.java.getResourceAsStream("/$fileName")?.use {
                    file.outputStream().use { out -> it.copyTo(out) }
                }
            }
            file.createNewFile()
        }
        return YamlConfiguration.loadConfiguration(file)
    }
}