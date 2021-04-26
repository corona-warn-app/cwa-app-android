package de.rki.coronawarnapp.ui.presencetracing.organizer.list.items

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsItemBinding
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.BaseCheckInVH.Companion.setupMenu
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.TraceLocationsAdapter
import de.rki.coronawarnapp.util.list.SwipeConsumer
import org.joda.time.format.DateTimeFormat

class TraceLocationVH(parent: ViewGroup) :
    TraceLocationsAdapter.ItemVH<TraceLocationVH.Item, TraceLocationOrganizerTraceLocationsItemBinding>(
        layoutRes = R.layout.trace_location_organizer_trace_locations_item,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationOrganizerTraceLocationsItemBinding> = lazy {
        TraceLocationOrganizerTraceLocationsItemBinding.bind(itemView)
    }

    override val onBindData: TraceLocationOrganizerTraceLocationsItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        description.text = item.traceLocation.description
        address.text = item.traceLocation.address

        if (item.traceLocation.startDate != null && item.traceLocation.endDate != null) {

            val startTime = item.traceLocation.startDate.toDateTime()
            val endTime = item.traceLocation.endDate.toDateTime()

            duration.isGone = false
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
                icon.setCaption(null)
                val dateTimeFormat = DateTimeFormat.shortDateTime()
                context.getString(
                    R.string.trace_location_organizer_list_item_duration,
                    startTime.toString(dateTimeFormat),
                    endTime.toString(dateTimeFormat)
                )
            }
        } else {
            icon.setCaption(null)
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
    ) : TraceLocationItem, SwipeConsumer {
        override val stableId: Long = traceLocation.id.hashCode().toLong()
        override fun onSwipe(position: Int, direction: Int) = onSwipeItem(traceLocation, position)
    }
}
