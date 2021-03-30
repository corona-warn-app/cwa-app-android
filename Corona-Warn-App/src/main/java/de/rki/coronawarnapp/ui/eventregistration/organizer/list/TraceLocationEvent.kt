package de.rki.coronawarnapp.ui.eventregistration.organizer.list

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.CheckInEvent

sealed class TraceLocationEvent {

    data class ConfirmDeleteItem(val traceLocation: TraceLocation) : TraceLocationEvent()

    data class ConfirmSwipeItem(val traceLocation: TraceLocation, val position: Int) : TraceLocationEvent()

    data class OpenDetailItem(val guid: String) : TraceLocationEvent()

}
