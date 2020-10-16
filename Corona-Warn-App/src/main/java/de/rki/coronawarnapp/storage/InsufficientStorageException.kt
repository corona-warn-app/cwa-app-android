package de.rki.coronawarnapp.storage

import android.content.Context
import android.text.format.Formatter
import de.rki.coronawarnapp.util.FormattedError
import java.io.IOException

class InsufficientStorageException(
    val result: DeviceStorage.CheckResult
) : IOException(
    "Not enough free space: ${result.requiredBytes}B are required and only ${result.freeBytes}B are available."
), FormattedError {

    override fun getFormattedError(context: Context): String {
        val formattedRequired = Formatter.formatShortFileSize(context, result.requiredBytes)
        val formattedFree = Formatter.formatShortFileSize(context, result.freeBytes)
        // TODO Replace with localized message when the exception is logged via new error tracking.
        return "Not enough free space: $formattedRequired are required and only $formattedFree are available."
    }
}
