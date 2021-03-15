package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import android.os.Parcelable
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import kotlinx.parcelize.Parcelize
import org.joda.time.Instant

@Parcelize
data class VerifiedTraceLocation(
    val guid: String,
    val version: Int,
    val type: TraceLocationOuterClass.TraceLocationType,
    val description: String,
    val address: String,
    val start: Instant?,
    val end: Instant?,
    val defaultCheckInLengthInMinutes: Int,
) : Parcelable
