package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class LogUpload(
    @JsonProperty("id") val id: String,
    @JsonProperty("uploadedAt") val uploadedAt: Instant
)
