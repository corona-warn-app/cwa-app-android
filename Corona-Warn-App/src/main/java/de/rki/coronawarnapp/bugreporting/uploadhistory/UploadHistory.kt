package de.rki.coronawarnapp.bugreporting.uploadhistory

data class UploadHistory(
    val logs: List<LogUpload> = emptyList()
)
