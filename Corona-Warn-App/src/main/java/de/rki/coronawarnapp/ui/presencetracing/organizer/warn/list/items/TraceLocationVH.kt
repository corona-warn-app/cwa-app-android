package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items

import android.view.ViewGroup
import androidx.core.view.isInvisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsWarnItemBinding
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.TraceLocationSelectionAdapter
import org.joda.time.format.DateTimeFormat

class TraceLocationVH(parent: ViewGroup) :
    TraceLocationSelectionAdapter.ItemVH<TraceLocationVH.Item, TraceLocationOrganizerTraceLocationsWarnItemBinding>(
        layoutRes = R.layout.trace_location_organizer_trace_locations_warn_item,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationOrganizerTraceLocationsWarnItemBinding> = lazy {
        TraceLocationOrganizerTraceLocationsWarnItemBinding.bind(itemView)
    }

    override val onBindData: TraceLocationOrganizerTraceLocationsWarnItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        address.text = item.traceLocation.address
        title.text = item.traceLocation.description
        headerCheckbox.setImageResource(if (item.selected) R.drawable.ic_selected else R.drawable.ic_unselected)

        if (item.traceLocation.startDate != null && item.traceLocation.endDate != null) {

            val startTime = item.traceLocation.startDate.toDateTime()
            val endTime = item.traceLocation.endDate.toDateTime()

            duration.isInvisible = false
            duration.text = if (startTime.toLocalDate() == endTime.toLocalDate()) {
                val dateFormat = DateTimeFormat.shortDate()
                val timeFormat = DateTimeFormat.shortTime()
                context.getString(
                    R.string.trace_location_organizer_list_item_duration_same_day,
                    startTime.toString(dateFormat),
                    startTime.toString(timeFormat),
                    endTime.toString(timeFormat)
                )
            } else {
                val dateTimeFormat = DateTimeFormat.shortDateTime()
                context.getString(
                    R.string.trace_location_organizer_list_item_duration,
                    startTime.toString(dateTimeFormat),
                    endTime.toString(dateTimeFormat)
                )
            }
        } else {
            duration.isInvisible = true
        }

        itemView.apply {
            setOnClickListener { item.onCardClicked(item.traceLocation) }
        }
    }

    data class Item(
        val traceLocation: TraceLocation,
        val selected: Boolean,
        val onCardClicked: (TraceLocation) -> Unit
    ) : TraceLocationItem {
        override val stableId: Long = traceLocation.id.hashCode().toLong()
    }
}
