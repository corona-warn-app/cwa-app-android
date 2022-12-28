package de.rki.coronawarnapp.ui.presencetracing.organizer.list

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation

sealed class TraceLocationEvent {

    data class DuplicateItem(val traceLocation: TraceLocation) : TraceLocationEvent()

    data class SelfCheckIn(val traceLocation: TraceLocation, val isOnboarded: Boolean) : TraceLocationEvent()

    data class ConfirmDeleteItem(val traceLocation: TraceLocation) : TraceLocationEvent()

    data class ConfirmSwipeItem(val traceLocation: TraceLocation, val position: Int) : TraceLocationEvent()

    data class StartQrCodeDetailFragment(val id: Long, val position: Int) : TraceLocationEvent()

    data class StartQrCodePosterFragment(val traceLocation: TraceLocation) : TraceLocationEvent()
}
