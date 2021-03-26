package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemPastBinding
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.list.SwipeConsumer
import org.joda.time.format.DateTimeFormat

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

        val checkInStartUserTZ = curItem.checkin.checkInStart.toUserTimeZone()
        val checkInEndUserTZ = curItem.checkin.checkInEnd.toUserTimeZone()

        description.text = curItem.checkin.description
        address.text = curItem.checkin.address

        checkoutInfo.text = run {
            val dayFormatted = checkInStartUserTZ.toLocalDate().toString(DateTimeFormat.mediumDate())
            val startTimeFormatted = checkInStartUserTZ.toLocalTime().toString(DateTimeFormat.shortTime())
            val endTimeFormatted = checkInEndUserTZ.toLocalTime().toString(DateTimeFormat.shortTime())

            "$dayFormatted, $startTimeFormatted - $endTimeFormatted"
        }

        menuAction.setupMenu(R.menu.menu_trace_location_attendee_checkin_item) {
            when (it.itemId) {
                R.id.menu_remove_item -> curItem.onRemoveItem(curItem.checkin).let { true }
                else -> false
            }
        }

        itemView.setOnClickListener { curItem.onCardClicked(curItem.checkin) }
    }

    data class Item(
        val checkin: CheckIn,
        val onCardClicked: (CheckIn) -> Unit,
        val onRemoveItem: (CheckIn) -> Unit,
        val onSwipeItem: (CheckIn, Int) -> Unit,
    ) : CheckInsItem, HasPayloadDiffer, SwipeConsumer {
        override val stableId: Long = checkin.id

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null

        override fun onSwipe(position: Int, direction: Int) = onSwipeItem(checkin, position)
    }
}
