package me.korolkotov.blackauction.config

import org.bukkit.configuration.ConfigurationSection

class MessageConfig(section: ConfigurationSection) {
    val prefix = section.getString("prefix")!!

    val helpConfig = HelpConfig(section.getConfigurationSection("help")!!)
    val errorsConfig = ErrorsConfig(section.getConfigurationSection("errors")!!)
    val successConfig = SuccessConfig(section.getConfigurationSection("success")!!)
    val scannerConfig = ScannerConfig(section.getConfigurationSection("scanner")!!)
    val warningsConfig = WarningsConfig(section.getConfigurationSection("warnings")!!)
    val notifications = NotificationsConfig(section.getConfigurationSection("notifications")!!)
}

class HelpConfig(private val section: ConfigurationSection) {
    val header = section.getString("header")!!

    fun getMessage(key: String) = section.getString(key)!!
}

class ErrorsConfig(section: ConfigurationSection) {
    val notEnoughPerms = section.getString("not-enough-perms")!!
    val notEnoughArgs = section.getString("not-enough-args")!!
    val onlyForPlayer = section.getString("only-for-player")!!
    val somethingWentWrong = section.getString("something-went-wrong")!!
    val wrongNumber = section.getString("wrong-number")!!
    val inventoryIsFull = section.getString("inventory-is-full")!!
    val claimsIsEmpty = section.getString("claims-is-empty")!!
}

class SuccessConfig(section: ConfigurationSection) {
    val claimedSome = section.getString("claimed-some")!!
    val claimedAll = section.getString("claimed-all")!!
}

class ScannerConfig(section: ConfigurationSection) {
    val specifyNumber = section.getString("specify-number")!!
}

class WarningsConfig(section: ConfigurationSection) {
    val notEnoughMoney = section.getString("not-enough-money")!!
    val notMinBid = section.getString("not-min-bid")!!
}

class NotificationsConfig(section: ConfigurationSection) {
    val auctionStarting = section.getString("auction-starting")!!
    val auctionStarted = section.getString("auction-started")!!
    val auctionEningSoon = section.getString("auction-ending-soon")!!
    val youWereOutbid = section.getString("you-were-outbid")!!
    val youWon = section.getString("you-won")!!
    val auctionExtended = section.getString("auction-extended")!!
}