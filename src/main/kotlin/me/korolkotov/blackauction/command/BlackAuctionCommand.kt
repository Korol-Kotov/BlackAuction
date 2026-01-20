package me.korolkotov.blackauction.command

import me.korolkotov.blackauction.annotations.SubCommand
import org.bukkit.command.CommandSender

class BlackAuctionCommand : CommandExecutor() {
    @SubCommand(commands = ["help"], permissionNode = "help")
    fun help(sender: CommandSender, label: String) {
        sendHelpMessage(sender, label)
    }
}