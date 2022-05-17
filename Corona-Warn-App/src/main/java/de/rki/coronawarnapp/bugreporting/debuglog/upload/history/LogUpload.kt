package de.rki.coronawarnapp.bugreporting.debuglog.upload.history

import java.time.Instant

data class LogUpload(
    val id: String,
    val uploadedAt: Instant
)
