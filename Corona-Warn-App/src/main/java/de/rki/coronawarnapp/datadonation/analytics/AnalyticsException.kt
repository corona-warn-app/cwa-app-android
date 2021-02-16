package de.rki.coronawarnapp.datadonation.analytics

class AnalyticsException(
    message: String?,
    cause: Throwable? = null
) : Exception(message, cause)
