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
    val commands = CommandsConfig(section.getConfigurationSection("commands")!!)
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
    val maxLots = section.getString("max-lots")!!
    val cantChangeLot = section.getString("cant-change-lot")!!
    val wrongDate = section.getString("wrong-date")!!
    val playerNotFound = section.getString("player-not-found")!!
    val lotNotFound = section.getString("lot-not-found")!!
    val wrongEconomyType = section.getString("wrong-economy-type")!!
    val alreadyEconomyType = section.getString("already-economy-type")!!
    val economyTypeNotRegistered = section.getString("economy-type-not-registered")!!
}

class SuccessConfig(section: ConfigurationSection) {
    val claimedSome = section.getString("claimed-some")!!
    val claimedAll = section.getString("claimed-all")!!
    val bidPlaced = section.getString("bid-placed")!!
}

class ScannerConfig(section: ConfigurationSection) {
    val specifyNumber = section.getString("specify-number")!!
    val specifyDate = section.getString("specify-date")!!
    val specifyPlayer = section.getString("specify-player")!!
    val specifyEconomyType = section.getString("specify-economy-type")!!
}

class WarningsConfig(section: ConfigurationSection) {
    val notEnoughMoney = section.getString("not-enough-money")!!
    val notMinBid = section.getString("not-min-bid")!!
    val noActiveLots = section.getString("no-active-lots")!!
}

class NotificationsConfig(section: ConfigurationSection) {
    val auctionStarting = section.getString("auction-starting")!!
    val auctionStarted = section.getString("auction-started")!!
    val auctionEningSoon = section.getString("auction-ending-soon")!!
    val youWereOutbid = section.getString("you-were-outbid")!!
    val youWon = section.getString("you-won")!!
    val auctionExtended = section.getString("auction-extended")!!
}

class CommandsConfig(section: ConfigurationSection) {
    val cancel = section.getString("cancel")!!
    val end = section.getString("end")!!
    val reload = section.getString("reload")!!
}