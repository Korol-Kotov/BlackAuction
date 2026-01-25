package me.korolkotov.blackauction.menu.impls

import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.auction.model.LotStatus
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.ItemButton
import me.korolkotov.blackauction.menu.button.MenuButton
import me.korolkotov.blackauction.menu.button.SimpleButton
import me.korolkotov.blackauction.scanner.ScannerManager
import me.korolkotov.blackauction.util.ItemBuilder
import me.korolkotov.blackauction.util.MessageService
import me.korolkotov.blackauction.util.TimeUtil
import me.korolkotov.blackauction.util.format
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.PlayerInventory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AdminLotMenu(
    val lot: Lot
) : Menu("admin-lot-menu") {
    val scanner get() = LoadManager.getInstance(ScannerManager::class.java)

    override fun canDrag(slot: Int) = false

    override fun initButtons(player: Player) {
        config.items.filter { it.getMenu() != null }.forEach { item ->
            val menu = getFromId(item.getMenu()!!, player) ?: return@forEach
            addButton(MenuButton(
                item.getItem()!!,
                item.getSlots(),
                menu
            ))
        }

        val lotInfo = config.getItem("lot-put")
        addButton(ItemButton(
            lotInfo.getItem()!!,
            lotInfo.getSlots()
        ))

        val lotItem = config.getItem("lot-item")
        addButton(SimpleButton(
            { lot.item.clone() },
            lotItem.getSlots(),
            {}
        ))

        val startPrice = config.getItem("start-price")
        addButton(SimpleButton(
            {
                val lore = MessageService.format(startPrice.getLore(),
                    mapOf("%start_price%" to lot.startPrice.toString()))
                ItemBuilder(startPrice.getItem()!!).lore(lore).build()
            },
            startPrice.getSlots()
        ) { data ->
            if (lot.status != LotStatus.PLANNED) {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.cantChangeLot)
                return@SimpleButton
            }

            MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.scannerConfig.specifyNumber)
            data.player.closeInventory()
            scanner.waitFor(data.player, 20L * 30) { message ->
                if (lot.status != LotStatus.PLANNED) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.cantChangeLot)
                    return@waitFor
                }

                val num = message.toDoubleOrNull()
                if (num == null || num <= 0) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.wrongNumber)
                    return@waitFor
                }

                lot.startPrice = num
                update()
                this.open(data.player)
            }
        })

        val minBetStep = config.getItem("min-bet-step")
        addButton(SimpleButton(
            {
                val lore = MessageService.format(minBetStep.getLore(),
                    mapOf("%min_bet%" to lot.minStep.toString()))
                ItemBuilder(minBetStep.getItem()!!).lore(lore).build()
            },
            minBetStep.getSlots()
        ) { data ->
            if (lot.status != LotStatus.PLANNED) {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.cantChangeLot)
                return@SimpleButton
            }

            MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.scannerConfig.specifyNumber)
            data.player.closeInventory()
            scanner.waitFor(data.player, 20L * 30) { message ->
                if (lot.status != LotStatus.PLANNED) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.cantChangeLot)
                    return@waitFor
                }

                val num = message.toDoubleOrNull()
                if (num == null || num <= 0) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.wrongNumber)
                    return@waitFor
                }

                lot.minStep = num
                update()
                this.open(data.player)
            }
        })

        val startTime = config.getItem("start-time")
        addButton(SimpleButton(
            {
                val lore = MessageService.format(startTime.getLore(),
                    mapOf("%start_time%" to lot.startTime.format(ConfigManager.instance.config.auction.general.dateFormat)))
                ItemBuilder(startTime.getItem()!!).lore(lore).build()
            },
            startTime.getSlots()
        ) { data ->
            if (lot.status != LotStatus.PLANNED) {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.cantChangeLot)
                return@SimpleButton
            }

            MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.scannerConfig.specifyDate)
            data.player.closeInventory()
            scanner.waitFor(data.player, 20L * 30) { message ->
                if (lot.status != LotStatus.PLANNED) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.cantChangeLot)
                    return@waitFor
                }

                val date = parsePlayerDateToInstant(message)
                val now = TimeUtil.now()
                if (date == null || date.isBefore(now)) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.wrongDate)
                    return@waitFor
                }

                lot.startTime = date
                update()
                this.open(data.player)
            }
        })

        val endTime = config.getItem("end-time")
        addButton(SimpleButton(
            {
                val lore = MessageService.format(endTime.getLore(),
                    mapOf("%end_time%" to lot.endTime.format(ConfigManager.instance.config.auction.general.dateFormat)))
                ItemBuilder(endTime.getItem()!!).lore(lore).build()
            },
            endTime.getSlots()
        ) { data ->
            if (lot.status != LotStatus.PLANNED) {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.cantChangeLot)
                return@SimpleButton
            }

            MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.scannerConfig.specifyDate)
            data.player.closeInventory()
            scanner.waitFor(data.player, 20L * 30) { message ->
                if (lot.status != LotStatus.PLANNED) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.cantChangeLot)
                    return@waitFor
                }

                val date = parsePlayerDateToInstant(message)
                if (date == null || date.isBefore(lot.startTime)) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.wrongDate)
                    return@waitFor
                }

                lot.endTime = date
                update()
                this.open(data.player)
            }
        })
    }

    override fun onClickOutside(event: InventoryClickEvent) {
        val inv = event.clickedInventory ?: return
        if (inv !is PlayerInventory) return
        val item = event.currentItem ?: return
        if (item.type.isEmpty) return

        lot.item = item.clone()
        update()
    }

    private fun parsePlayerDateToInstant(playerInput: String): Instant? {
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern(ConfigManager.instance.config.auction.general.dateFormat)
            val localDateTime = LocalDateTime.parse(playerInput, formatter)
            val moscowZone = ZoneId.of("Europe/Moscow")
            val zonedMoscow = localDateTime.atZone(moscowZone)
            zonedMoscow.toInstant()
        }.getOrNull()
    }
}