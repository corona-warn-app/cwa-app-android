package de.rki.coronawarnapp.bugreporting.debuglog.upload.history

data class UploadHistory(
    val logs: List<LogUpload> = emptyList()
)
