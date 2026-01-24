package me.korolkotov.blackauction.util

import java.time.Clock

object TimeUtil {
    fun now() = Clock.systemUTC().instant()!!
}