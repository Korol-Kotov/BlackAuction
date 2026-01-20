package me.korolkotov.blackauction.command

import me.korolkotov.blackauction.load.LoadManagerInterface
import me.korolkotov.blackauction.logger.Logger
import org.bukkit.Bukkit

class CommandManager : LoadManagerInterface<CommandManager> {
    override fun getInstance() = this

    override fun initialize() {
        val command = Bukkit.getPluginCommand("blackauction")
        if (command != null) {
            val executor = BlackAuctionCommand()

            command.setExecutor(executor)
            command.tabCompleter = executor
            Logger.instance.debug("Command ${command.name} has been registered with executor ${executor::class.simpleName}.")
        }
    }
}