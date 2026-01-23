package me.korolkotov.blackauction.scanner

import io.papermc.paper.event.player.AsyncChatEvent
import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.load.LoadManagerInterface
import me.korolkotov.blackauction.util.TaskService
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ScannerManager : LoadManagerInterface<ScannerManager>, Listener {
    private val scans = mutableListOf<Scan>()

    override fun getInstance() = this

    override fun initialize() {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)

        TaskService.runTimer("scan-check", 0, 20L) {
            val now = System.currentTimeMillis() / 1000
            scans.toList().filter { now > it.end }.forEach(scans::remove)
        }
    }

    fun waitFor(player: Player, delay: Long, runnable: (String) -> Unit) {
        scans.removeAll { it.player.uniqueId == player.uniqueId }

        val scan = Scan(player, System.currentTimeMillis() / 1000 + delay, runnable)
        scans.add(scan)
    }

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val message = LegacyComponentSerializer.legacySection().serialize(event.message())
        if (message.startsWith('/')) return
        val scans = this.scans.filter { it.player.uniqueId == event.player.uniqueId }
        if (scans.isEmpty()) return

        event.isCancelled = true
        scans.forEach { scan ->
            scan.runnable(message)
            this.scans.remove(scan)
        }
    }

    private data class Scan(
        val player: Player,
        val end: Long,
        val runnable: (String) -> Unit
    )
}