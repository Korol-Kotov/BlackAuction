package me.korolkotov.blackauction.economy

import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.load.LoadManagerInterface
import me.korolkotov.blackauction.logger.Logger
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class EconomyManager : LoadManagerInterface<EconomyManager> {
    companion object {
        lateinit var instance: EconomyManager private set
    }

    private lateinit var economy: Economy

    init {
        instance = this
    }

    override fun getInstance() = this

    override fun initialize() {
        if (!initEconomy()) {
            Logger.instance.error("Vault dependency wasn't found, disabling plugin.")
            Bukkit.getPluginManager().disablePlugin(Main.instance)
            return
        }
    }

    private fun initEconomy(): Boolean {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return false
        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return false
        economy = rsp.provider
        return true
    }

    fun has(player: OfflinePlayer, amount: Double): Boolean =
        economy.has(player, amount)

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return true

        val response = economy.withdrawPlayer(player, amount)
        Logger.instance.debug("$amount was withdrawn from ${player.name}'s account (transaction success: ${response.transactionSuccess()})")
        return response.transactionSuccess()
    }

    fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return true

        val response = economy.depositPlayer(player, amount)
        Logger.instance.debug("$amount was deposited to ${player.name}'s account (transaction success: ${response.transactionSuccess()})")
        return response.transactionSuccess()
    }

    fun format(amount: Double): String =
        economy.format(amount)
}