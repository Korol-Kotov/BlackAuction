package me.korolkotov.blackauction.load

import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.coroutine.DatabaseDispatcher
import me.korolkotov.blackauction.coroutine.PluginCoroutineScope
import me.korolkotov.blackauction.database.DatabaseManager
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
        loadManagerInterfaces.add(DatabaseManager())
    }

    fun initialize() = loadManagerInterfaces.forEach { it.initialize() }

    fun terminate() {
        loadManagerInterfaces.reversed().forEach { it.terminate() }
        PluginCoroutineScope.shutdown()
        DatabaseDispatcher.IO.close()
    }

    fun reload() {
        loadManagerInterfaces.reversed().forEach { it.reload() }
        MessageService.reloadMessages()
    }
}