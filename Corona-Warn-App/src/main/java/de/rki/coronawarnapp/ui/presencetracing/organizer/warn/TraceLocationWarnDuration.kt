package de.rki.coronawarnapp.ui.presencetracing.organizer.warn

import android.os.Parcelable
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import kotlinx.parcelize.Parcelize
import org.joda.time.Duration
import org.joda.time.LocalDateTime

@Parcelize
data class TraceLocationWarnDuration(
    val traceLocation: TraceLocation,
    val dateTime: LocalDateTime,
    val duration: Duration
) : Parcelable
