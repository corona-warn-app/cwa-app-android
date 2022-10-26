package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items

import android.view.ViewGroup
import androidx.core.view.isInvisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsWarnItemBinding
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.TraceLocationSelectionAdapter
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

        warnItemAddress.text = item.traceLocation.address
        warnItemTitle.text = item.traceLocation.description
        headerCheckbox.setImageResource(if (item.selected) R.drawable.ic_selected else R.drawable.ic_unselected)

        if (item.traceLocation.startDate != null && item.traceLocation.endDate != null) {

            val startTime = item.traceLocation.startDate.toLocalDateTimeUserTz()
            val endTime = item.traceLocation.endDate.toLocalDateTimeUserTz()

            duration.isInvisible = false
            duration.text = if (startTime.toLocalDate() == endTime.toLocalDate()) {
                val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                context.getString(
                    R.string.trace_location_organizer_list_item_duration_same_day,
                    startTime.format(dateFormat),
                    startTime.format(timeFormat),
                    endTime.format(timeFormat)
                )
            } else {
                val dateTimeFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                context.getString(
                    R.string.trace_location_organizer_list_item_duration,
                    startTime.format(dateTimeFormat),
                    endTime.format(dateTimeFormat)
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
