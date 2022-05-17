package de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth

import java.time.Instant

data class LogUploadOtp(
    val otp: String,
    val expirationDate: Instant
)
