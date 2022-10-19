package de.rki.coronawarnapp.ui.presencetracing.organizer.list.items

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsItemBinding
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.BaseCheckInVH.Companion.setupMenu
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.TraceLocationsAdapter
import de.rki.coronawarnapp.util.list.Swipeable
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TraceLocationVH(parent: ViewGroup) :
    TraceLocationsAdapter.ItemVH<TraceLocationVH.Item, TraceLocationOrganizerTraceLocationsItemBinding>(
        layoutRes = R.layout.trace_location_organizer_trace_locations_item,
        parent = parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.traceLocation, holder.bindingAdapterPosition) }
    }

    override val viewBinding: Lazy<TraceLocationOrganizerTraceLocationsItemBinding> = lazy {
        TraceLocationOrganizerTraceLocationsItemBinding.bind(itemView)
    }

    override val onBindData: TraceLocationOrganizerTraceLocationsItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        latestItem = item

        traceLocationsItemDescription.text = item.traceLocation.description
        traceLocationsItemAddress.text = item.traceLocation.address

        if (item.traceLocation.startDate != null && item.traceLocation.endDate != null) {

            val startTime = item.traceLocation.startDate.atZone(ZoneId.systemDefault())
            val endTime = item.traceLocation.endDate.atZone(ZoneId.systemDefault())

            val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

            duration.isGone = false
            duration.text = if (startTime.toLocalDate() == endTime.toLocalDate()) {
                context.getString(
                    R.string.trace_location_organizer_list_item_duration_same_day,
                    startTime.format(dateFormat),
                    startTime.format(timeFormat),
                    endTime.format(timeFormat)
                )
            } else {
                traceLocationsItemIcon.setCaption(null)
                val startDateTime =
                    "${startTime.toLocalDate().format(dateFormat)}, ${startTime.toLocalTime().format(timeFormat)}"
                val endDateTime =
                    "${endTime.toLocalDate().format(dateFormat)}, ${endTime.toLocalTime().format(timeFormat)}"
                context.getString(
                    R.string.trace_location_organizer_list_item_duration,
                    startDateTime,
                    endDateTime
                )
            }
        } else {
            traceLocationsItemIcon.setCaption(null)
            duration.isGone = true
        }

        menuAction.setupMenu(R.menu.menu_trace_location_organizer_item) {
            when (it.itemId) {
                R.id.menu_duplicate -> item.onDuplicate(item.traceLocation).let { true }
                R.id.menu_show_print -> item.onShowPrint(item.traceLocation).let { true }
                R.id.menu_clear -> item.onDeleteItem(item.traceLocation).let { true }
                else -> false
            }
        }

        checkinAction.isVisible = item.canCheckIn
        checkinAction.setOnClickListener { item.onCheckIn(item.traceLocation) }
        itemView.apply {
            setOnClickListener { item.onCardClicked(item.traceLocation, bindingAdapterPosition) }
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
    ) : TraceLocationItem {
        override val stableId: Long = traceLocation.id.hashCode().toLong()
    }
}
