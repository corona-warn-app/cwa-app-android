package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemPastBinding
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
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
    ) -> Unit = { item, _ ->
        val checkInStartUserTZ = item.checkin.checkInStart.toUserTimeZone()
        val checkInEndUserTZ = item.checkin.checkInEnd.toUserTimeZone()

        description.text = item.checkin.description
        address.text = item.checkin.address

        checkoutInfo.text = run {
            val dayFormatted = checkInStartUserTZ.toLocalDate().toString(DateTimeFormat.mediumDate())
            val startTimeFormatted = checkInStartUserTZ.toLocalTime().toString(DateTimeFormat.shortTime())
            val endTimeFormatted = checkInEndUserTZ.toLocalTime().toString(DateTimeFormat.shortTime())

            "$dayFormatted, $startTimeFormatted - $endTimeFormatted"
        }

        menuAction.setupMenu(R.menu.menu_trace_location_attendee_checkin_item) {
            when (it.itemId) {
                R.id.menu_remove_item -> item.onRemoveItem(item.checkin).let { true }
                else -> false
            }
        }
    }

    data class Item(
        val checkin: CheckIn,
        val onCardClicked: (CheckIn) -> Unit,
        val onRemoveItem: (CheckIn) -> Unit
    ) : CheckInsItem {
        override val stableId: Long = checkin.id
    }
}
