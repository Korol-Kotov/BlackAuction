package me.korolkotov.blackauction.load

import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.command.CommandManager
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.coroutine.PluginCoroutineScope
import me.korolkotov.blackauction.database.DatabaseManager
import me.korolkotov.blackauction.economy.EconomyManager
import me.korolkotov.blackauction.logger.LoggerManager
import me.korolkotov.blackauction.menu.MenuManager
import me.korolkotov.blackauction.scanner.ScannerManager
import me.korolkotov.blackauction.util.MessageService

class LoadManager {
    companion object {
        private val loadManagerInterfaces = mutableListOf<LoadManagerInterface<*>>()

        fun <T> getInstance(clazz: Class<T>): T {
            return loadManagerInterfaces.filterIsInstance(clazz).firstNotNullOfOrNull { (it as LoadManagerInterface<T>).getInstance() }!!
        }
    }

    init {
        loadManagerInterfaces.add(ConfigManager())
        loadManagerInterfaces.add(LoggerManager())
        loadManagerInterfaces.add(DatabaseManager())
        loadManagerInterfaces.add(EconomyManager())
        loadManagerInterfaces.add(AuctionManager())
        loadManagerInterfaces.add(ScannerManager())
        loadManagerInterfaces.add(MenuManager())
        loadManagerInterfaces.add(CommandManager())
    }

    fun initialize() = loadManagerInterfaces.forEach { it.initialize() }

    fun terminate() = loadManagerInterfaces.reversed().forEach { it.terminate() }

    fun reload() = loadManagerInterfaces.reversed().forEach { it.reload() }
}