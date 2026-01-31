package me.korolkotov.blackauction.economy

import me.korolkotov.blackauction.logger.Logger
import org.bukkit.OfflinePlayer
import su.nightexpress.coinsengine.api.CoinsEngineAPI
import su.nightexpress.coinsengine.api.currency.Currency

class CoinsEngineEconomy(
    private val currency: Currency
) : Economy {
    override fun has(player: OfflinePlayer, amount: Double): Boolean =
        CoinsEngineAPI.getBalance(player.uniqueId, currency) >= amount

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return true

        val response = CoinsEngineAPI.removeBalance(player.uniqueId, currency, amount)
        Logger.instance.debug("[CoinsEngine] $amount was withdrawn from ${player.name}'s account (transaction success: $response)")
        return response
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return true

        val response = CoinsEngineAPI.addBalance(player.uniqueId, currency, amount)
        Logger.instance.debug("[CoinsEngine] $amount was deposited to ${player.name}'s account (transaction success: $response)")
        return response
    }
}