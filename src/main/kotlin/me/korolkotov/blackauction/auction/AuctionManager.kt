package me.korolkotov.blackauction.auction

import kotlinx.coroutines.launch
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.auction.model.LotStatus
import me.korolkotov.blackauction.config.ConfigManager
import me.korolkotov.blackauction.coroutine.PluginCoroutineScope
import me.korolkotov.blackauction.database.DatabaseManager
import me.korolkotov.blackauction.database.repository.AuctionRepository
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.load.LoadManagerInterface
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Clock
import java.time.temporal.ChronoUnit

class AuctionManager : LoadManagerInterface<AuctionManager> {
    companion object {
        lateinit var instance: AuctionManager private set
    }

    val cache = AuctionCache()
    val bidProcessor = BidProcessor()

    lateinit var repository: AuctionRepository private set
    lateinit var scheduler: AuctionScheduler private set

    init {
        instance = this
    }

    override fun getInstance() = this

    override fun initialize() {
        repository = LoadManager.getInstance(DatabaseManager::class.java).auctionRepository
        scheduler = AuctionScheduler(this)
        scheduler.startScheduler()

        PluginCoroutineScope.scope.launch {
            repository.lotDao.findActiveOrPlanned(Clock.systemUTC().instant()).forEach { lot ->
                cache.put(lot.slot, lot)
            }
        }
    }

    fun canCreateLot(): Boolean =
        cache.getLots().filter { it.status == LotStatus.PLANNED || it.status == LotStatus.RUNNING }.size < ConfigManager.instance.config.auction.general.maxLots

    fun createLot(creator: Player): Lot {
        val config = ConfigManager.instance.config.auction
        val now = Clock.systemUTC().instant()
        val start = now.plus(1, ChronoUnit.DAYS)
        val end = start.plus(1, ChronoUnit.HOURS)
        val lot = Lot(
            0,
            cache.nextSlot(),
            ItemStack(Material.AIR),
            config.bidding.defaultStartPrice,
            config.bidding.defaultBidStep,
            start,
            end,
            LotStatus.PLANNED,
            creator.uniqueId,
            now,
            0.0,
            null
        )
        cache.put(lot.slot, lot)
        PluginCoroutineScope.scope.launch { repository.lotDao.create(lot) }
        return lot
    }
}