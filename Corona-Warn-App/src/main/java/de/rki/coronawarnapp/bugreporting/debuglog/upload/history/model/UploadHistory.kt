package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model

import com.fasterxml.jackson.annotation.JsonProperty

data class UploadHistory(
    @JsonProperty("logs") val logs: List<LogUpload> = emptyList()
)
