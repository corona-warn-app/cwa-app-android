package de.rki.coronawarnapp.bugreporting.debuglog

data class LogState(
    val isLogging: Boolean,
    val isLowStorage: Boolean,
    val logSize: Long
)
