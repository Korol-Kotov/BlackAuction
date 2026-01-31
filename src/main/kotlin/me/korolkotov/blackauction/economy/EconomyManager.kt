package me.korolkotov.blackauction.economy

import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.load.LoadManagerInterface
import me.korolkotov.blackauction.logger.Logger
import me.korolkotov.blackauction.util.format
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import su.nightexpress.coinsengine.api.CoinsEngineAPI
import java.math.BigDecimal
import java.math.RoundingMode

class EconomyManager : LoadManagerInterface<EconomyManager> {
    companion object {
        lateinit var instance: EconomyManager private set
    }

    private var vaultEconomy: VaultEconomy? = null
    private var coinsEngineEconomy: CoinsEngineEconomy? = null

    init {
        instance = this
    }

    override fun getInstance() = this

    override fun initialize() {
        if (!initVault()) {
            Logger.instance.error("Vault dependency wasn't found, disabling plugin.")
            Bukkit.getPluginManager().disablePlugin(Main.instance)
            return
        }

        if (!initCoinsEngine()) {
            Logger.instance.warn("CoinsEngine dependency wasn't found.")
        }
    }

    private fun initVault(): Boolean {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return false
        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return false
        vaultEconomy = VaultEconomy(rsp.provider)
        return true
    }

    private fun initCoinsEngine(): Boolean {
        if (!Bukkit.getPluginManager().isPluginEnabled("CoinsEngine")) return false
        val currency = CoinsEngineAPI.getCurrency(ConfigManager.instance.config.auction.economy.coinsEngineCurrency) ?: return false
        coinsEngineEconomy = CoinsEngineEconomy(currency)
        return true
    }

    fun isRegistered(type: EconomyType): Boolean = when (type) {
        EconomyType.VAULT -> vaultEconomy != null
        EconomyType.COINS_ENGINE -> coinsEngineEconomy != null
    }

    fun has(type: EconomyType, player: OfflinePlayer, amount: Double): Boolean = when (type) {
        EconomyType.VAULT -> vaultEconomy?.has(player, amount)
        EconomyType.COINS_ENGINE -> coinsEngineEconomy?.has(player, amount)
    } ?: false

    fun withdraw(type: EconomyType, player: OfflinePlayer, amount: Double): Boolean = when (type) {
        EconomyType.VAULT -> vaultEconomy?.withdraw(player, amount)
        EconomyType.COINS_ENGINE -> coinsEngineEconomy?.withdraw(player, amount)
    } ?: false

    fun deposit(type: EconomyType, player: OfflinePlayer, amount: Double): Boolean = when (type) {
        EconomyType.VAULT -> vaultEconomy?.deposit(player, amount)
        EconomyType.COINS_ENGINE -> coinsEngineEconomy?.deposit(player, amount)
    } ?: false

    fun format(type: EconomyType, amount: Double): String = when (type) {
        EconomyType.VAULT -> amount.format() + ConfigManager.instance.config.auction.economy.symbols.vault
        EconomyType.COINS_ENGINE -> amount.format() + ConfigManager.instance.config.auction.economy.symbols.coinsEngine
    }
}

enum class EconomyType {
    VAULT,
    COINS_ENGINE;
}