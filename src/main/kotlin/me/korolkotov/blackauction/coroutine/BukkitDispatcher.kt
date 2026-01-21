package me.korolkotov.blackauction.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import me.korolkotov.blackauction.Main
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

object BukkitDispatcher {
    val MAIN = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            Bukkit.getScheduler().runTask(Main.instance, block)
        }
    }
}