package de.rki.coronawarnapp.exception

import okio.IOException

class CwaWebSecurityException(cause: Throwable) : IOException(
    "an error occurred while trying to establish a secure connection to the server",
    cause
)
