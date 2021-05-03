package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.vaccination.core.VaccinationException

class VaccinationCertificateNotFoundException(
    message: String
) : VaccinationException(
    message = message,
    cause = null
)
