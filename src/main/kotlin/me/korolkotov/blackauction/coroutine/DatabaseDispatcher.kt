package me.korolkotov.blackauction.coroutine

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object DatabaseDispatcher {
    val IO = Executors
        .newFixedThreadPool(4)
        .asCoroutineDispatcher()
}
