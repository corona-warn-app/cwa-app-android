package de.rki.coronawarnapp.test.api.ui

import de.rki.coronawarnapp.util.CWADebug

data class LoggerState(
    val isLogging: Boolean
) {
    companion object {
        internal fun CWADebug.toLoggerState() = LoggerState(
            isLogging = fileLogger?.isLogging ?: false
        )
    }
}
