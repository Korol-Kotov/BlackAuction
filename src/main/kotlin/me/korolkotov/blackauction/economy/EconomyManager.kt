package me.korolkotov.blackauction.economy

import me.korolkotov.blackauction.load.LoadManagerInterface
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
        val rsp = Bukkit.getServicesManager()
            .getRegistration(Economy::class.java)
            ?: error("Vault economy provider not found")

        economy = rsp.provider
    }

    fun has(player: OfflinePlayer, amount: Double): Boolean =
        economy.has(player, amount)

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return true

        val response = economy.withdrawPlayer(player, amount)
        return response.transactionSuccess()
    }

    fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return true

        val response = economy.depositPlayer(player, amount)
        return response.transactionSuccess()
    }

    fun format(amount: Double): String =
        economy.format(amount)
}