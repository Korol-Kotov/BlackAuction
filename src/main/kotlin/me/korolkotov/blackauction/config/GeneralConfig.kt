package me.korolkotov.blackauction.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

class GeneralConfig(yaml: YamlConfiguration) {
    val plugin = PluginConfig(yaml.getConfigurationSection("plugin")!!)
    val auction = AuctionConfig(yaml.getConfigurationSection("auction")!!)
}

class PluginConfig(section: ConfigurationSection) {
    val language = section.getString("language")!!
    val debug = section.getBoolean("debug")
}

class AuctionConfig(section: ConfigurationSection) {
    val general = AuctionGeneralConfig(section.getConfigurationSection("general")!!)
    val bidding = AuctionBiddingConfig(section.getConfigurationSection("bidding")!!)
    val notifications = AuctionNotificationsConfig(section.getConfigurationSection("notifications")!!)
}

class AuctionGeneralConfig(section: ConfigurationSection) {
    val maxLots = section.getInt("max-lots")
    val dateFormat = section.getString("date-format")!!
    val timeFormat = section.getString("time-format")!!
}

class AuctionBiddingConfig(section: ConfigurationSection) {
    val defaultStartPrice = section.getDouble("default-start-price")
    val defaultBidStep = section.getDouble("default-bid-step")
}

class AuctionNotificationsConfig(section: ConfigurationSection) {
    val warnBeforeStart = section.getIntegerList("warn-before-start")
    val warnLeaderBeforeEnd = section.getIntegerList("warn-leader-before-end")
}