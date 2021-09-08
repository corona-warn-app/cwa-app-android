package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.duration

import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.TraceLocationWarnDuration

sealed class TraceLocationWarnDurationEvent {

    data class ContinueWithTraceLocationDuration(val traceLocationWarnDuration: TraceLocationWarnDuration) :
        TraceLocationWarnDurationEvent()
}
