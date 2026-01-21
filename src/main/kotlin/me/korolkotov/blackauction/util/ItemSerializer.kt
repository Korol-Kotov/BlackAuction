package me.korolkotov.blackauction.util

import org.bukkit.inventory.ItemStack
import java.util.*

object ItemSerializer {
    fun serialize(item: ItemStack): String {
        val bytes = item.serializeAsBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun deserialize(data: String): ItemStack {
        val bytes = Base64.getDecoder().decode(data)
        return ItemStack.deserializeBytes(bytes)
    }
}