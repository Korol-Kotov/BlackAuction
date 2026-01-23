package me.korolkotov.blackauction.auction

import kotlinx.coroutines.launch
import me.korolkotov.blackauction.auction.cache.AuctionCache
import me.korolkotov.blackauction.auction.cache.ClaimCache
import me.korolkotov.blackauction.auction.cache.LotHistoryCache
import me.korolkotov.blackauction.auction.cache.PlayerHistoryCache
import me.korolkotov.blackauction.auction.model.Claim
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.auction.model.LotHistory
import me.korolkotov.blackauction.auction.model.LotStatus
import me.korolkotov.blackauction.auction.model.PlayerHistory
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

    val auctionCache = AuctionCache()
    val claimsCache = ClaimCache()
    val playerHistoryCache = PlayerHistoryCache()
    val lotHistoryCache = LotHistoryCache()

    lateinit var repository: AuctionRepository private set
    lateinit var scheduler: AuctionScheduler private set
    lateinit var bidProcessor: BidProcessor private set

    init {
        instance = this
    }

    override fun getInstance() = this

    override fun initialize() {
        repository = LoadManager.getInstance(DatabaseManager::class.java).auctionRepository
        scheduler = AuctionScheduler(this)
        scheduler.startScheduler()
        bidProcessor = BidProcessor(this)

        PluginCoroutineScope.scope.launch {
            repository.lotDao.findActiveOrPlanned(Clock.systemUTC().instant()).forEach { lot ->
                auctionCache.put(lot.slot, lot)
            }

            lotHistoryCache.addAll(repository.lotHistoryDao.findRecent(500))
        }
    }

    fun canCreateLot(): Boolean =
        auctionCache.getLots().filter { it.status == LotStatus.PLANNED || it.status == LotStatus.RUNNING }.size < ConfigManager.instance.config.auction.general.maxLots

    fun createLot(creator: Player): Lot {
        val config = ConfigManager.instance.config.auction
        val now = Clock.systemUTC().instant()
        val start = now.plus(1, ChronoUnit.DAYS)
        val end = start.plus(1, ChronoUnit.HOURS)
        val lot = Lot(
            0,
            auctionCache.nextSlot(),
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
        auctionCache.put(lot.slot, lot)
        PluginCoroutineScope.scope.launch { repository.lotDao.create(lot) }
        return lot
    }

    fun getClaims(player: Player): List<Claim> {
        if (!claimsCache.has(player.uniqueId)) {
            claimsCache.put(player.uniqueId, emptyList())
            PluginCoroutineScope.scope.launch {
                claimsCache.put(player.uniqueId, repository.claimDao.findByPlayer(player.uniqueId))
            }
        }

        val list = claimsCache.get(player.uniqueId) ?: emptyList()
        return list
    }

    fun getHistory(player: Player): List<PlayerHistory> {
        if (!playerHistoryCache.has(player.uniqueId)) {
            playerHistoryCache.put(player.uniqueId, emptyList())
            PluginCoroutineScope.scope.launch {
                playerHistoryCache.put(player.uniqueId, repository.playerHistoryDao.findByPlayer(player.uniqueId))
            }
        }

        val list = playerHistoryCache.get(player.uniqueId) ?: emptyList()
        return list
    }
}