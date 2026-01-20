package me.korolkotov.blackauction

import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.logger.Logger
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
        Logger.instance.debug("Plugin has enabled.")
    }

    override fun onDisable() {
        Logger.instance.debug("Disabling plugin.")
        loadManager.terminate()
        logger.info("Plugin $name disabled!")
    }
}