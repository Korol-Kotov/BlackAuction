package me.korolkotov.blackauction.menu.impls

import me.korolkotov.blackauction.auction.AuctionManager
import me.korolkotov.blackauction.auction.model.Lot
import me.korolkotov.blackauction.load.LoadManager
import me.korolkotov.blackauction.menu.Menu
import me.korolkotov.blackauction.menu.button.SimpleButton
import org.bukkit.entity.Player

class AdminLotCancelMenu(
    private val lot: Lot
) : Menu("admin-lot-cancel-menu") {
    private val auctionManager get() = LoadManager.getInstance(AuctionManager::class.java)

    override fun canDrag(slot: Int) = false

    override fun initButtons(player: Player) {
        val item = config.getItem("lot")
        addButton(SimpleButton(
            { lot.item.clone() },
            item.getSlots(),
            {}
        ))

        val agree = config.getItem("agree")
        addButton(SimpleButton(
            { agree.getItem()!!.clone() },
            agree.getSlots()
        ) { data ->
            auctionManager.scheduler.cancel(lot)
            val menu = AdminMenu()
            menu.open(data.player)
        })

        val cancel = config.getItem("cancel")
        addButton(SimpleButton(
            { cancel.getItem()!!.clone() },
            cancel.getSlots()
        ) { data ->
            val menu = AdminMenu()
            menu.open(data.player)
        })
    }
}