package de.rki.coronawarnapp.covidcertificate.vaccination.core

open class VaccinationException(
    cause: Throwable?,
    message: String
) : Exception(message, cause)
