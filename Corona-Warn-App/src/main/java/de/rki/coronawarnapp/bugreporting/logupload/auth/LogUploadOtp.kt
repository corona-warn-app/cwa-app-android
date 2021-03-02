package de.rki.coronawarnapp.bugreporting.logupload.auth

import org.joda.time.Instant

data class LogUploadOtp(
    val otp: String,
    val expirationDate: Instant
)
