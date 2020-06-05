package de.rki.coronawarnapp.exception

import java.lang.Exception

class CwaSecurityException(cause: Throwable) : Exception(
    "something went wrong during a critical part of the application ensuring security, please refer" +
            "to the details for more information",
    cause
)
