package de.rki.coronawarnapp.vaccination.core

open class VaccinationException(
    cause: Throwable?,
    message: String
) : Exception(message, cause)
