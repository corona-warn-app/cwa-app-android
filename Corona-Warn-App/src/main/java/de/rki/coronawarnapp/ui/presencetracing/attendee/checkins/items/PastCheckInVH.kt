package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemPastBinding
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.common.checkoutInfo
import de.rki.coronawarnapp.util.list.SwipeConsumer
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PastCheckInVH(parent: ViewGroup) :
    BaseCheckInVH<PastCheckInVH.Item, TraceLocationAttendeeCheckinsItemPastBinding>(
        layoutRes = R.layout.trace_location_attendee_checkins_item_past,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationAttendeeCheckinsItemPastBinding> = lazy {
        TraceLocationAttendeeCheckinsItemPastBinding.bind(itemView)
    }

    override val onBindData: TraceLocationAttendeeCheckinsItemPastBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        description.text = curItem.checkin.description
        address.text = curItem.checkin.address

        checkoutInfo.text = curItem.checkin.checkoutInfo

        menuAction.setupMenu(R.menu.menu_trace_location_attendee_checkin_item) {
            when (it.itemId) {
                R.id.menu_remove_item -> curItem.onRemoveItem(curItem.checkin).let { true }
                else -> false
            }
        }

        itemView.apply {
            setOnClickListener { curItem.onCardClicked(curItem.checkin, adapterPosition) }
            transitionName = item.checkin.id.toString()
        }
    }

    data class Item(
        val checkin: CheckIn,
        val onCardClicked: (CheckIn, Int) -> Unit,
        val onRemoveItem: (CheckIn) -> Unit,
        val onSwipeItem: (CheckIn, Int) -> Unit,
    ) : CheckInsItem, HasPayloadDiffer, SwipeConsumer {
        override val stableId: Long = checkin.id

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null

        override fun onSwipe(position: Int, direction: Int) = onSwipeItem(checkin, position)
    }
}
