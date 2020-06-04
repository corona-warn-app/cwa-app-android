package de.rki.coronawarnapp.exception

class NoTokenException(
    cause: Throwable
) : Exception(
    "An error occurred during BroadcastReceiver onReceive function. No token found",
    cause
)
