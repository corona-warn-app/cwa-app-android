package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsWarnItemBinding
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.TraceLocationsWarnAdapter
import org.joda.time.format.DateTimeFormat

class TraceLocationVH(parent: ViewGroup) :
    TraceLocationsWarnAdapter.ItemVH<TraceLocationVH.Item, TraceLocationOrganizerTraceLocationsWarnItemBinding>(
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

        if (item.traceLocation.startDate != null && item.traceLocation.endDate != null) {

            val startTime = item.traceLocation.startDate.toDateTime()
            val endTime = item.traceLocation.endDate.toDateTime()

            duration.isVisible = false
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
            duration.isVisible = true
        }

        itemView.apply {
            setOnClickListener { item.onCardClicked(item.traceLocation, adapterPosition) }
            transitionName = item.traceLocation.id.toString()
        }
    }

    data class Item(
        val traceLocation: TraceLocation,
        val canCheckIn: Boolean,
        val onCheckIn: (TraceLocation) -> Unit,
        val onDuplicate: (TraceLocation) -> Unit,
        val onShowPrint: (TraceLocation) -> Unit,
        val onDeleteItem: (TraceLocation) -> Unit,
        val onSwipeItem: (TraceLocation, Int) -> Unit,
        val onCardClicked: (TraceLocation, Int) -> Unit
    ) : TraceLocationWarnItem {
        override val stableId: Long = traceLocation.id.hashCode().toLong()
    }
}
