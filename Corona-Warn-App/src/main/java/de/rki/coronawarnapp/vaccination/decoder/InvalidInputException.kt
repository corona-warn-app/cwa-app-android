package de.rki.coronawarnapp.vaccination.decoder

class InvalidInputException(
    message: String = "An error occurred while decoding input."
) : Exception(message)
