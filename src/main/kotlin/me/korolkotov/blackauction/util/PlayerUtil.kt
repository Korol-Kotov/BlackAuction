package me.korolkotov.blackauction.util

import org.bukkit.Bukkit
import java.util.UUID

object PlayerUtil {
    fun resolvePlayerName(uuid: UUID, default: String = "Undefined"): String {
        Bukkit.getPlayer(uuid)?.let { return it.name }
        Bukkit.getOfflinePlayer(uuid).name?.let { return it }
        return default
    }
}