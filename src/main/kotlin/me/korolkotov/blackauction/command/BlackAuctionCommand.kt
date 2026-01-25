package me.korolkotov.blackauction.command

import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.annotations.SubCommand
import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.auction.model.LotStatus
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.impls.*
import me.korolkotov.blackauction.util.MessageService
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BlackAuctionCommand : CommandExecutor() {
    val auctionManager get() = LoadManager.getInstance(AuctionManager::class.java)
    
    @SubCommand(commands = ["help"], permissionNode = "help")
    fun help(sender: CommandSender) {
        sendHelpMessage(sender)
    }

    @SubCommand(permissionNode = "use")
    fun use(player: Player) {
        val active = auctionManager.auctionCache.getLots().filter { it.status == LotStatus.RUNNING }
        if (active.isEmpty()) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.warningsConfig.noActiveLots)
            return
        }

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
        val lot = am.auctionCache.get(slot - 1)
        if (lot == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.lotNotFound)
            return
        }

        am.scheduler.cancel(lot)
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commands.cancel,
            mapOf("%slot%" to slot.toString()))
    }

    @SubCommand(commands = ["end"], permissionNode = "admin.end")
    fun end(player: Player, slot: Int) {
        val am = auctionManager
        val lot = am.auctionCache.get(slot - 1)
        if (lot == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.lotNotFound)
            return
        }

        am.scheduler.end(lot)
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commands.end,
            mapOf("%slot%" to slot.toString()))
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