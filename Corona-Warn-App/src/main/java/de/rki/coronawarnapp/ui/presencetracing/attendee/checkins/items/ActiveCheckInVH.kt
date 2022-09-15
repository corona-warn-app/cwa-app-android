package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemActiveBinding
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.toJoda
import de.rki.coronawarnapp.util.toUserTimeZone
import org.joda.time.DurationFieldType
import org.joda.time.PeriodType
import org.joda.time.format.PeriodFormat
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.TimeUnit

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

    private val hourPeriodFormatter by lazy {
        PeriodFormat.wordBased(context.getLocale())
    }

    override val onBindData: TraceLocationAttendeeCheckinsItemActiveBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        latestItem = curItem

        val checkInStartUserTZ = curItem.checkin.checkInStart.toUserTimeZone()

        highlightDuration.text = run {
            val currentDuration = Duration.between(checkInStartUserTZ, Instant.now().toUserTimeZone())
            val saneDuration = if (currentDuration < Duration.ZERO) Duration.ZERO else currentDuration
            val seconds = saneDuration.toMinutes() * 60
            "%02d:%02d".format(seconds / SECONDS_IN_HOURS, (seconds % SECONDS_IN_HOURS) / 60)
        }

        description.text = curItem.checkin.description
        address.text = curItem.checkin.address

        checkoutInfo.text = run {
            val checkoutIn = Duration.between(curItem.checkin.checkInStart, curItem.checkin.checkInEnd).let {
                val periodType = when {
                    it > Duration.ofHours(1) -> PeriodType.forFields(
                        arrayOf(DurationFieldType.hours(), DurationFieldType.minutes())
                    )
                    it > Duration.ofDays(1) -> PeriodType.days()
                    else -> PeriodType.minutes()
                }
                it.toJoda().toPeriod(periodType)
            }

            val startDate = checkInStartUserTZ.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            val startTime = checkInStartUserTZ.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            context.getString(
                R.string.trace_location_checkins_card_automatic_checkout_info_format,
                startDate,
                startTime,
                hourPeriodFormatter.print(checkoutIn)
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

    companion object {
        private val SECONDS_IN_HOURS = TimeUnit.HOURS.toSeconds(1)
    }
}
