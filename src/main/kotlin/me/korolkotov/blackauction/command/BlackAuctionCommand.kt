package me.korolkotov.blackauction.command

import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.annotations.SubCommand
import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.impls.AdminHistoryMenu
import me.korolkotov.blackauction.menu.impls.AdminMenu
import me.korolkotov.blackauction.menu.impls.ClaimsMenu
import me.korolkotov.blackauction.menu.impls.HistoryMenu
import me.korolkotov.blackauction.menu.impls.MainMenu
import me.korolkotov.blackauction.util.MessageService
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BlackAuctionCommand : CommandExecutor() {
    val auctionManager get() = LoadManager.getInstance(AuctionManager::class.java)
    
    @SubCommand(commands = ["help"], permissionNode = "help")
    fun help(sender: CommandSender, label: String) {
        sendHelpMessage(sender, label)
    }

    @SubCommand(permissionNode = "use")
    fun use(player: Player) {
        val menu = MainMenu()
        menu.open(player)
    }

    @SubCommand(commands = ["claims"], permissionNode = "claims")
    fun claims(player: Player) {
        val menu = ClaimsMenu(player, 1)
        menu.open(player)
    }

    @SubCommand(commands = ["myhistory"], permissionNode = "myhistory")
    fun myHistory(player: Player) {
        val menu = HistoryMenu(player, 1)
        menu.open(player)
    }

    @SubCommand(commands = ["admin"], permissionNode = "admin")
    fun admin(player: Player) {
        val menu = AdminMenu()
        menu.open(player)
    }

    @SubCommand(commands = ["cancel"], permissionNode = "admin.cancel")
    fun cancel(player: Player, slot: Int) {
        val am = auctionManager
        val lot = am.auctionCache.get(slot)
        if (lot == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.lotNotFound)
            return
        }

        am.scheduler.cancel(lot)
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commands.cancel)
    }

    @SubCommand(commands = ["end"], permissionNode = "admin.end")
    fun end(player: Player, slot: Int) {
        val am = auctionManager
        val lot = am.auctionCache.get(slot)
        if (lot == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.lotNotFound)
            return
        }

        am.scheduler.end(lot)
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commands.end)
    }

    @SubCommand(commands = ["history"], permissionNode = "admin.history")
    fun history(sender: Player, player: OfflinePlayer?) {
        if (player == null) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.playerNotFound)
            return
        }

        val menu = AdminHistoryMenu(1, player)
        menu.open(sender)
    }

    @SubCommand(commands = ["reload"], permissionNode = "admin.reload")
    fun reload(sender: Player) {
        Main.instance.loadManager.reload()
        MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.commands.reload)
    }
}