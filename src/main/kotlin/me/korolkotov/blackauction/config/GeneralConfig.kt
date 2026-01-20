package me.korolkotov.blackauction.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

class GeneralConfig(yaml: YamlConfiguration) {
    val plugin = PluginConfig(yaml.getConfigurationSection("plugin")!!)
}

class PluginConfig(section: ConfigurationSection) {
    val language = section.getString("language")!!
    val debug = section.getBoolean("debug")
}