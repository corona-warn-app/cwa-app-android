package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.errors

import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationException

class VaccinationCertificateNotFoundException(
    message: String
) : VaccinationException(
    message = message,
    cause = null
)
