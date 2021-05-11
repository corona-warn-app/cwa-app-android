package de.rki.coronawarnapp.util.compression

class InvalidInputException(
    message: String = "An error occurred while decoding input."
) : Exception(message)
