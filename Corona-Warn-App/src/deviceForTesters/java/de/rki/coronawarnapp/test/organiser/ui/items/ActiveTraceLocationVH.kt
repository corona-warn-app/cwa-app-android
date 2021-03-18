package de.rki.coronawarnapp.test.organiser.ui.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerQrCodesListItemBinding
import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import de.rki.coronawarnapp.test.organiser.ui.TraceLocationsAdapter
import kotlin.time.hours

class ActiveTraceLocationVH (parent: ViewGroup) :
    TraceLocationsAdapter.ItemVH<ActiveTraceLocationVH.Item, TraceLocationOrganizerQrCodesListItemBinding>(
        layoutRes = R.layout.trace_location_organizer_qr_codes_list_item,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationOrganizerQrCodesListItemBinding> = lazy {
        TraceLocationOrganizerQrCodesListItemBinding.bind(itemView)
    }

    override val onBindData: TraceLocationOrganizerQrCodesListItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        description.text = item.traceLocation.description
        address.text = item.traceLocation.address

        // TODO: define what to do with duration and icon
        val durationText = "${item.traceLocation.startDate?.toDateTime()?.hourOfDay?.hours.toString()} -" +
            " ${item.traceLocation.startDate?.toDateTime()?.hourOfDay?.hours.toString()}"
        duration.text = durationText
        val iconDateText = item.traceLocation.startDate?.toDateTime()?.toString("dd.MM.yy")
        if (iconDateText != null) {
            icon.setCaption(iconDateText)
        }
    }

    data class Item(
        val traceLocation: TraceLocation
    ) : TraceLocationItem {
        override val stableId: Long = traceLocation.guid.hashCode().toLong()
    }
}
