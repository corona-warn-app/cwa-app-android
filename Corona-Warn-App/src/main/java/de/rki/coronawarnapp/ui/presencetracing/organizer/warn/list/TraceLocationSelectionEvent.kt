package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation

sealed class TraceLocationSelectionEvent {

    data class ContinueWithTraceLocation(val traceLocation: TraceLocation) : TraceLocationSelectionEvent()

    object ScanQrCode : TraceLocationSelectionEvent()
}
