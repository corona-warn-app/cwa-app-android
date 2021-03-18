package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemActiveBinding
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.PeriodType
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormat
import org.joda.time.format.PeriodFormatterBuilder

class ActiveCheckInVH(parent: ViewGroup) :
    BaseCheckInVH<ActiveCheckInVH.Item, TraceLocationAttendeeCheckinsItemActiveBinding>(
        layoutRes = R.layout.trace_location_attendee_checkins_item_active,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationAttendeeCheckinsItemActiveBinding> = lazy {
        TraceLocationAttendeeCheckinsItemActiveBinding.bind(itemView)
    }

    private val hourPeriodFormatter by lazy {
        PeriodFormat.wordBased(context.getLocale())
    }

    override val onBindData: TraceLocationAttendeeCheckinsItemActiveBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val checkInStartUserTZ = item.checkin.checkInStart.toUserTimeZone()

        val checkinDuration = Duration(checkInStartUserTZ, Instant.now())
        highlightDuration.text = highlightDurationForamtter.print(checkinDuration.toPeriod())

        description.text = item.checkin.description
        address.text = item.checkin.address
        val startDate = checkInStartUserTZ.toLocalDate()
        traceLocationCardHighlightView.setCaption(startDate.toString(DateTimeFormat.mediumDate()))

        val autoCheckoutText = item.checkin.defaultCheckInLengthInMinutes?.let { checkoutLength ->
            val checkoutAt = checkInStartUserTZ.plus(Duration.standardMinutes(checkoutLength.toLong()))
            val checkoutIn = Duration(Instant.now(), checkoutAt).let {
                val periodType = when {
                    it.isLongerThan(Duration.standardHours(1)) -> PeriodType.hours()
                    it.isLongerThan(Duration.standardDays(1)) -> PeriodType.days()
                    else -> PeriodType.minutes()
                }
                it.toPeriod(periodType)
            }

            context.getString(
                R.string.trace_location_checkins_card_automatic_checkout_info,
                checkInStartUserTZ.toLocalTime().toString("HH:mm"),
                hourPeriodFormatter.print(checkoutIn)
            )
        }

        checkoutInfo.text = autoCheckoutText ?: checkInStartUserTZ.toLocalTime().toString("HH:mm")

        menuAction.setupMenu(R.menu.menu_trace_location_attendee_checkin_item) {
            when (it.itemId) {
                R.id.menu_remove_item -> item.onRemoveItem(item.checkin).let { true }
                else -> false
            }
        }
    }

    data class Item(
        val checkin: CheckIn,
        val onCardClicked: (CheckIn) -> Unit,
        val onRemoveItem: (CheckIn) -> Unit,
        val onCheckout: (CheckIn) -> Unit,
    ) : CheckInsItem {
        override val stableId: Long = checkin.id
    }

    companion object {
        private val highlightDurationForamtter = PeriodFormatterBuilder().apply {
            printZeroAlways()
            minimumPrintedDigits(2)
            appendHours()
            appendSuffix(":")
            appendMinutes()
        }.toFormatter()
    }
}
