package de.rki.coronawarnapp.logging

import java.util.Date

@Deprecated("better idea now")
data class LogElement(
    val message: String,
    val priority: Int,
    val tag: String? = null,
    val date: Date = Date(),
    val t: Throwable? = null
)
