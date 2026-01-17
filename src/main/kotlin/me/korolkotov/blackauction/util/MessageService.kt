package me.korolkotov.blackauction.util

import me.korolkotov.blackauction.Main
import me.korolkotov.blackauction.config.ConfigManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.regex.Pattern

object MessageService {
    private val language get() = ConfigManager.instance.config.getString("plugin.language") ?: "en"
    private val pluginFolder get() = Main.instance.dataFolder

    private var messages: YamlConfiguration

    init {
        messages = loadLanguageFile(language)
    }

    private fun loadLanguageFile(language: String): YamlConfiguration {
        val langFile = File(pluginFolder, "messages/$language.yml")
        if (!langFile.exists()) {
            pluginFolder.resolve("messages").mkdirs()
            this::class.java.getResourceAsStream("/messages/$language.yml")?.use {
                langFile.outputStream().use { out -> it.copyTo(out) }
            }
        }
        return YamlConfiguration.loadConfiguration(langFile)
    }

    fun getMessage(key: String, replacements: Map<String, String> = emptyMap()): String {
        val rawMessage = messages.getString("messages.$key")
            ?: return "Message not found: $key"

        return format(rawMessage, replacements)
    }

    fun getList(key: String, replacements: Map<String, String> = emptyMap()): List<String> {
        return format(messages.getStringList("messages.$key"), replacements)
    }

    fun format(text: String, replacements: Map<String, String> = emptyMap()): String {
        var result = text
        replacements.forEach { (key, value) -> result = result.replace(key, value) }

        val hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})")
        val matcher = hexPattern.matcher(result)
        val buffer = StringBuffer()

        while (matcher.find()) {
            val hex = matcher.group(1)
            val replacement = StringBuilder("§x")
            hex.toCharArray().forEach { char -> replacement.append('§').append(char) }
            matcher.appendReplacement(buffer, replacement.toString())
        }
        matcher.appendTail(buffer)

        return ChatColor.translateAlternateColorCodes('&', buffer.toString())
    }

    fun format(list: List<String>, replacements: Map<String, String> = emptyMap()): List<String> {
        return list.map { format(it, replacements) }
    }

    fun sendMessage(sender: CommandSender, key: String, replacements: Map<String, String> = emptyMap(), isList: Boolean = false) {
        if (!isList) {
            val message = getMessage(key, replacements)
            sender.sendMessage(message)
        } else {
            val messages = getList(key, replacements)
            messages.forEach(sender::sendMessage)
        }
    }

    fun broadcast(key: String, replacements: Map<String, String> = emptyMap(), isList: Boolean = false) {
        Bukkit.getOnlinePlayers().forEach { player -> sendMessage(player, key, replacements, isList) }
    }

    fun reloadMessages() {
        messages = loadLanguageFile(language)
    }
}