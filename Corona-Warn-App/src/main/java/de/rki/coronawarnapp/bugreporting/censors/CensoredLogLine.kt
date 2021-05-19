package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine

data class CensoredLogLine(
    val censored: LogLine,
    val censoredRange: IntRange
)
