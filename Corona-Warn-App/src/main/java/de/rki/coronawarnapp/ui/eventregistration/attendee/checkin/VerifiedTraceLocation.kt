package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import android.os.Parcelable
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifyResult
import kotlinx.parcelize.Parcelize
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import java.util.concurrent.TimeUnit

@Parcelize
data class VerifiedTraceLocation(
    val guid: String,
    val description: String?,
    val start: Instant?,
    val end: Instant?,
    val defaultCheckInLengthInMinutes: Int,
    // TODO add required properties to confirm check-in
) : Parcelable

fun QRCodeVerifyResult.toVerifiedTraceLocation() =
    with(singedTraceLocation.event) {
        VerifiedTraceLocation(
            guid = guid.toByteArray().toByteString().base64(),
            start = start.instant(),
            end = end.instant(),
            description = description,
            defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes
        )
    }

/**
 * Converts time in seconds into [Instant]
 */
private fun Int.instant() =
    if (this == 0) null else Instant.ofEpochMilli(TimeUnit.SECONDS.toMillis(toLong()))
