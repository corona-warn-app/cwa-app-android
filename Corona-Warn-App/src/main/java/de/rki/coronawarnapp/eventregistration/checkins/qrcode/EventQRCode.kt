package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import org.joda.time.Instant

data class EventQRCode(
    val guid: String,
    val description: String? = null,
    val start: Instant? = null,
    val end: Instant? = null,
)
