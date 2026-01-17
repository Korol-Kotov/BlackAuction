package me.korolkotov.blackauction

import me.korolkotov.blackauction.load.LoadManager
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main private set
    }

    val loadManager = LoadManager()

    override fun onEnable() {
        instance = this
        loadManager.initialize()
        logger.info("Plugin $name enabled!")
    }

    override fun onDisable() {
        loadManager.terminate()
        logger.info("Plugin $name disabled!")
    }
}