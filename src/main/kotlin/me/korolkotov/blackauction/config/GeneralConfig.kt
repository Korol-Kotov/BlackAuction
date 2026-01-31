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
    val antiSnipe = AuctionAntiSnipeConfig(section.getConfigurationSection("anti-snipe")!!)
    val economy = AuctionEconomyConfig(section.getConfigurationSection("economy")!!)
    val bidding = AuctionBiddingConfig(section.getConfigurationSection("bidding")!!)
    val notifications = AuctionNotificationsConfig(section.getConfigurationSection("notifications")!!)
}

class AuctionAntiSnipeConfig(section: ConfigurationSection) {
    val enabled = section.getBoolean("enabled")
    val triggerSeconds = section.getInt("trigger-seconds")
    val extendBySeconds = section.getInt("extend-by-seconds")
    val maxExtensions = section.getInt("max-extensions")
}

class AuctionGeneralConfig(section: ConfigurationSection) {
    val maxLots = section.getInt("max-lots")
    val dateFormat = section.getString("date-format")!!
    val timeFormat = section.getString("time-format")!!
}

class AuctionEconomyConfig(section: ConfigurationSection) {
    val symbols = AuctionEconomySymbolsConfig(section.getConfigurationSection("symbols")!!)
    val coinsEngineCurrency = section.getString("coins-engine-currency")!!

    val commissionPercent = section.getDouble("commission-percent")
    val fixedCommission = section.getDouble("fixed-commission")
    val commissionDestination = section.getString("commission-destination")!!
    val treasuryAccount = section.getString("treasury-account")!!
}

class AuctionEconomySymbolsConfig(section: ConfigurationSection) {
    val vault = section.getString("vault")!!
    val coinsEngine = section.getString("coins-engine")!!
}

class AuctionBiddingConfig(section: ConfigurationSection) {
    val defaultStartPrice = section.getDouble("default-start-price")
    val defaultBidStep = section.getDouble("default-bid-step")
}

class AuctionNotificationsConfig(section: ConfigurationSection) {
    val warnBeforeStart = section.getIntegerList("warn-before-start")
    val warnLeaderBeforeEnd = section.getIntegerList("warn-leader-before-end")
}