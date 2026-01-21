package me.korolkotov.blackauction.config

import org.bukkit.configuration.ConfigurationSection

class MessageConfig(section: ConfigurationSection) {
    val prefix = section.getString("prefix")!!

    val helpConfig = HelpConfig(section.getConfigurationSection("help")!!)
    val errorsConfig = ErrorsConfig(section.getConfigurationSection("errors")!!)
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
}

class NotificationsConfig(section: ConfigurationSection) {
    val auctionStarting = section.getString("auction-starting")!!
    val auctionStarted = section.getString("auction-started")!!
    val auctionEningSoon = section.getString("auction-ending-soon")!!
    val youWereOutbid = section.getString("you-were-outbid")!!
    val youWon = section.getString("you-won")!!
    val auctionExtended = section.getString("auction-extended")!!
}