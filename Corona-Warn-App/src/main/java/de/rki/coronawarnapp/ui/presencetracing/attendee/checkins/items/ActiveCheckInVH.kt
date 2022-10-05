package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items

import android.text.format.DateUtils
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemActiveBinding
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.ui.durationpicker.format
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

        val checkin = curItem.checkin
        val checkInStart = checkin.checkInStart
        val checkInEnd = checkin.checkInEnd
        val checkInStartUserTZ = checkInStart.toLocalDateTimeUserTz()

        highlightDuration.text = run {
            val currentDuration = Duration.between(checkInStartUserTZ, LocalDateTime.now())
            val saneDuration = if (currentDuration < Duration.ZERO) Duration.ZERO else currentDuration
            saneDuration.format()
        }

        description.text = checkin.description
        address.text = checkin.address

        val checkoutIn = Duration.between(checkInStart, checkInEnd).let { duration ->
            when {
                duration > Duration.ofHours(1) -> "%s, %s".format(
                    relativeTime(
                        checkInStart.plus(Duration.ofHours(duration.toHoursPart().toLong())).toEpochMilli(),
                        checkInStart.toEpochMilli()
                    ),
                    relativeTime(
                        checkInStart.plus(Duration.ofMinutes(duration.toMinutesPart().toLong())).toEpochMilli(),
                        checkInStart.toEpochMilli()
                    )
                )

                else -> relativeTime(checkInEnd.toEpochMilli(), checkInStart.toEpochMilli())
            }
        }
        checkoutInfo.text = run {
            val startDate = checkInStartUserTZ.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            val startTime = checkInStartUserTZ.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            context.getString(
                R.string.trace_location_checkins_card_automatic_checkout_info_format,
                startDate,
                startTime,
                checkoutIn
            )
        }

        menuAction.setupMenu(R.menu.menu_trace_location_attendee_checkin_item) {
            when (it.itemId) {
                R.id.menu_remove_item -> curItem.onRemoveItem(checkin).let { true }
                else -> false
            }
        }

        checkoutAction.setOnClickListener { curItem.onCheckout(checkin) }

        itemView.apply {
            transitionName = item.checkin.id.toString()
        }
    }

    private fun relativeTime(time: Long, now: Long): String =
        DateUtils.getRelativeTimeSpanString(
            time,
            now,
            DateUtils.MINUTE_IN_MILLIS
        ).run {
            substring(indexOfFirst { it.isDigit() })
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
