package me.korolkotov.blackauction.auction.cache

import me.korolkotov.blackauction.auction.model.PlayerHistory
import java.util.*

class PlayerHistoryCache : Cache<UUID, List<PlayerHistory>>()