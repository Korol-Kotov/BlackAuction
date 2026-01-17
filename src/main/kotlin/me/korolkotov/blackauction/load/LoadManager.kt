package me.korolkotov.blackauction.load

import me.korolkotov.blackauction.config.ConfigManager

class LoadManager {
    companion object {
        private val loadManagerInterfaces = mutableListOf<LoadManagerInterface<*>>()

        fun <T> getInstance(clazz: Class<T>): T {
            return loadManagerInterfaces.filterIsInstance(clazz).firstNotNullOfOrNull { (it as LoadManagerInterface<T>).getInstance() }!!
        }
    }

    init {
        loadManagerInterfaces.add(ConfigManager())
    }

    fun initialize() = loadManagerInterfaces.forEach { it.initialize() }

    fun terminate() = loadManagerInterfaces.reversed().forEach { it.terminate() }

    fun reload() = loadManagerInterfaces.reversed().forEach { it.reload() }
}