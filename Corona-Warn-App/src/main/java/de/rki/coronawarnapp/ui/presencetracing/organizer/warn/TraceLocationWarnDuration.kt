package de.rki.coronawarnapp.ui.presencetracing.organizer.warn

import android.os.Parcelable
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
data class TraceLocationWarnDuration(
    val traceLocation: TraceLocation,
    val startDate: Instant,
    val endDate: Instant
) : Parcelable
