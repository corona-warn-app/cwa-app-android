package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation

sealed class TraceLocationWarnEvent {

    data class ContinueWithTraceLocation(val traceLocation: TraceLocation) : TraceLocationWarnEvent()
}
