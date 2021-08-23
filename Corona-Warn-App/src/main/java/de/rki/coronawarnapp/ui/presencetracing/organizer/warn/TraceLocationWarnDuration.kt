package de.rki.coronawarnapp.ui.presencetracing.organizer.warn

import android.os.Parcelable
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import kotlinx.parcelize.Parcelize
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDateTime

@Parcelize
data class TraceLocationWarnDuration(
    val traceLocation: TraceLocation,
    val startDate: Instant,
    val endDate: Instant
) : Parcelable
