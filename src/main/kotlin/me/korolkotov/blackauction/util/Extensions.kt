package me.korolkotov.blackauction.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.inventory.ItemStack
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun String.asComponent(): Component = LegacyComponentSerializer.legacyAmpersand().deserialize(this)

fun ResultSet.getInstant(column: String): Instant = getTimestamp(column).toInstant()

fun PreparedStatement.setInstant(index: Int, instant: Instant) = setTimestamp(index, Timestamp.from(instant))

fun ItemStack.getName(): String {
    return LegacyComponentSerializer.legacyAmpersand().serialize(itemMeta.displayName() ?: Component.translatable(translationKey()))
}

fun Instant.format(pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.of("UTC"))
    return formatter.format(this)
}

fun Instant.diffFormat(other: Instant, pattern: String): String {
    val diff = Duration.between(this, other)
    val hours = diff.toHours().toInt()
    val minutes = (diff.toMinutes() % 60).toInt()

    val time = LocalTime.of(hours, minutes)
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return formatter.format(time)
}