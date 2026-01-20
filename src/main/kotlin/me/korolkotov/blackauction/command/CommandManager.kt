package me.korolkotov.blackauction.command

import me.korolkotov.blackauction.load.LoadManagerInterface
import org.bukkit.Bukkit

class CommandManager : LoadManagerInterface<CommandManager> {
    override fun getInstance() = this

    override fun initialize() {
        val command = Bukkit.getPluginCommand("wellprivat")
        if (command != null) {
            val executor = BlackAuctionCommand()

            command.setExecutor(executor)
            command.tabCompleter = executor
        }
    }
}