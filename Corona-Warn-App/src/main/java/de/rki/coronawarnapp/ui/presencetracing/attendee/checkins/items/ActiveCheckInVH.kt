package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemActiveBinding
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.apache.commons.lang3.time.DurationFormatUtils
import java.text.DateFormat
import java.time.Duration
import java.time.Instant

// TODO: need to be improved
class ActiveCheckInVH(parent: ViewGroup) :
    BaseCheckInVH<ActiveCheckInVH.Item, TraceLocationAttendeeCheckinsItemActiveBinding>(
        layoutRes = R.layout.trace_location_attendee_checkins_item_active,
        parent = parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.checkin, holder.bindingAdapterPosition) }
    }

    override val viewBinding: Lazy<TraceLocationAttendeeCheckinsItemActiveBinding> = lazy {
        TraceLocationAttendeeCheckinsItemActiveBinding.bind(itemView)
    }

    override val onBindData: TraceLocationAttendeeCheckinsItemActiveBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        latestItem = curItem

        val checkInStartUserTZ = curItem.checkin.checkInStart.toUserTimeZone()

        highlightDuration.text = kotlin.run {
            val currentDuration = Duration.between(checkInStartUserTZ, Instant.now())
            val saneDuration = if (currentDuration < Duration.ZERO) {
                Duration.ZERO
            } else {
                currentDuration
            }
            "${saneDuration.toHours()}:${saneDuration.toMinutes()}"
        }

        description.text = curItem.checkin.description
        address.text = curItem.checkin.address

        checkoutInfo.text = run {
            val duration = Duration.between(curItem.checkin.checkInStart, curItem.checkin.checkInEnd)
            val periodType = duration.let {
                when {
                    it > Duration.ofHours(1) -> "HH:mm"
                    it > Duration.ofDays(1) -> "D"
                    else -> "mm"
                }
            }

            val startDate = DateFormat.getDateInstance(DateFormat.SHORT).format(checkInStartUserTZ)
            val startTime = DateFormat.getDateInstance(DateFormat.SHORT).format(checkInStartUserTZ)
            context.getString(
                R.string.trace_location_checkins_card_automatic_checkout_info_format,
                startDate,
                startTime,
                DurationFormatUtils.formatDuration(duration.toMillis(), periodType)
            )
        }

        menuAction.setupMenu(R.menu.menu_trace_location_attendee_checkin_item) {
            when (it.itemId) {
                R.id.menu_remove_item -> curItem.onRemoveItem(curItem.checkin).let { true }
                else -> false
            }
        }

        checkoutAction.setOnClickListener { curItem.onCheckout(curItem.checkin) }

        itemView.apply {
            transitionName = item.checkin.id.toString()
        }
    }

    data class Item(
        val checkin: CheckIn,
        val onCardClicked: (CheckIn, Int) -> Unit,
        val onRemoveItem: (CheckIn) -> Unit,
        val onCheckout: (CheckIn) -> Unit,
        val onSwipeItem: (CheckIn, Int) -> Unit,
    ) : CheckInsItem, HasPayloadDiffer {
        override val stableId: Long = checkin.id
    }
}
