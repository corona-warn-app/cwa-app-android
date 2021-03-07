package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import android.os.Parcelable
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.EventQRCode
import kotlinx.parcelize.Parcelize
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant

@Parcelize
data class VerifiedEvent(
    val guid: String,
    val description: String?,
    val start: Instant?,
    val end: Instant?,
    val defaultCheckInLengthInMinutes: Int
) : Parcelable

fun EventQRCode.toVerifiedEvent() = with(event) {

    VerifiedEvent(
        guid = guid.toByteArray().toByteString().base64(),
        start = start.instant(),
        end = end.instant(),
        description = description,
        defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes
    )
}

private fun Int.instant() =
    if (this == 0) null else Instant.ofEpochMilli(this.toLong())
