package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemActiveBinding
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.CheckInsAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import org.joda.time.format.DateTimeFormat

class ActiveCheckInVH(parent: ViewGroup) :
    CheckInsAdapter.ItemVH<ActiveCheckInVH.Item, TraceLocationAttendeeCheckinsItemActiveBinding>(
        layoutRes = R.layout.trace_location_attendee_checkins_item_active,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationAttendeeCheckinsItemActiveBinding> = lazy {
        TraceLocationAttendeeCheckinsItemActiveBinding.bind(itemView)
    }

    override val onBindData: TraceLocationAttendeeCheckinsItemActiveBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
//        highlightDuration.text =
        description.text = item.checkin.description
        address.text = item.checkin.address
        val startDate = item.checkin.checkInStart.toUserTimeZone().toLocalDate()
        traceLocationCardHighlightView.setCaption(startDate.toString(DateTimeFormat.mediumDate()))
    }

    data class Item(
        val checkin: CheckIn
    ) : CheckInsItem {
        override val stableId: Long = checkin.id
    }
}
