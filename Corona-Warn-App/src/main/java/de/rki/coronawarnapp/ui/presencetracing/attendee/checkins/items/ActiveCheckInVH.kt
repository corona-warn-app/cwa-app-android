package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemActiveBinding
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.Duration
import org.joda.time.DurationFieldType
import org.joda.time.Instant
import org.joda.time.PeriodType
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormat
import org.joda.time.format.PeriodFormatterBuilder

class ActiveCheckInVH(parent: ViewGroup) :
    BaseCheckInVH<ActiveCheckInVH.Item, TraceLocationAttendeeCheckinsItemActiveBinding>(
        layoutRes = R.layout.trace_location_attendee_checkins_item_active,
        parent = parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.checkin, holder.adapterPosition) }
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
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        latestItem = curItem

        val checkInStartUserTZ = curItem.checkin.checkInStart.toUserTimeZone()

        highlightDuration.text = kotlin.run {
            val currentDuration = Duration(checkInStartUserTZ, Instant.now())
            val saneDuration = if (currentDuration.isShorterThan(Duration.ZERO)) {
                Duration.ZERO
            } else {
                currentDuration
            }
            highlightDurationFormatter.print(saneDuration.toPeriod())
        }

        description.text = curItem.checkin.description
        address.text = curItem.checkin.address

        checkoutInfo.text = run {
            val checkoutIn = Duration(curItem.checkin.checkInStart, curItem.checkin.checkInEnd).let {
                val periodType = when {
                    it.isLongerThan(Duration.standardHours(1)) -> PeriodType.forFields(
                        arrayOf(DurationFieldType.hours(), DurationFieldType.minutes())
                    )
                    it.isLongerThan(Duration.standardDays(1)) -> PeriodType.days()
                    else -> PeriodType.minutes()
                }
                it.toPeriod(periodType)
            }

            val startDate = checkInStartUserTZ.toString(DateTimeFormat.shortDate())
            val startTime = checkInStartUserTZ.toString(DateTimeFormat.shortTime())
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

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }

    companion object {
        private val highlightDurationFormatter = PeriodFormatterBuilder().apply {
            printZeroAlways()
            minimumPrintedDigits(2)
            appendHours()
            appendSuffix(":")
            appendMinutes()
        }.toFormatter()
    }
}
