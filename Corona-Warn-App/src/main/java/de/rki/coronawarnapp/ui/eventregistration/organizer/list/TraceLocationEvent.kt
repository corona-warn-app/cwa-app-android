package de.rki.coronawarnapp.ui.eventregistration.organizer.list

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation

sealed class TraceLocationEvent {

    data class ConfirmDeleteItem(val traceLocation: TraceLocation) : TraceLocationEvent()

    data class ConfirmSwipeItem(val traceLocation: TraceLocation, val position: Int) : TraceLocationEvent()

    data class QrCodePrint(val traceLocation: TraceLocation) : TraceLocationEvent()
    data class QrCodeDetails(val traceLocation: TraceLocation) : TraceLocationEvent()
}
