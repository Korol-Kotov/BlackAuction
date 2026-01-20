package me.korolkotov.blackauction.config

import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.load.LoadManagerInterface
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager : LoadManagerInterface<ConfigManager> {
    companion object {
        lateinit var instance: ConfigManager private set
    }

    val dataFolder get() = Main.instance.dataFolder

    lateinit var config: GeneralConfig
    lateinit var databaseConfig: DatabaseConfig
    lateinit var messageConfig: MessageConfig

    init {
        instance = this
    }

    override fun getInstance() = this

    override fun initialize() {
        val config = loadOrCreate("config.yml")
        this.config = GeneralConfig(config)

        val database = loadOrCreate("database.yml")
        databaseConfig = DatabaseConfig(database)

        val languageFile = loadLanguageFile(this.config.plugin.language)
        messageConfig = MessageConfig(languageFile.getConfigurationSection("messages")!!)
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

    private fun loadLanguageFile(language: String): YamlConfiguration {
        return loadOrCreate("messages/$language.yml")
    }
}