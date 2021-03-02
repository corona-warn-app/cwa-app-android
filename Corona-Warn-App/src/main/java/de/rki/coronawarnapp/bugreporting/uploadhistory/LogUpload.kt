package de.rki.coronawarnapp.bugreporting.uploadhistory

import org.joda.time.Instant

data class LogUpload(
    val id: String,
    val uploadedAt: Instant
)
