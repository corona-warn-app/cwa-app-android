package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemPastBinding
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.CheckInsAdapter

class PastCheckInVH(parent: ViewGroup) :
    CheckInsAdapter.ItemVH<PastCheckInVH.Item, TraceLocationAttendeeCheckinsItemPastBinding>(
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

    }

    data class Item(
        val checkin: CheckIn
    ) : CheckInsItem {
        override val stableId: Long = checkin.id
    }
}
