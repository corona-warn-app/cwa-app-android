package de.rki.coronawarnapp.util.compression

class InvalidInputException(
    message: String = "An error occurred while decoding input.",
    cause: Throwable? = null,
) : Exception(message, cause)
