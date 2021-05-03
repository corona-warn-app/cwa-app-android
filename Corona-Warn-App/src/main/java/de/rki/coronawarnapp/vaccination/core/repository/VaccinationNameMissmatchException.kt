package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.vaccination.core.VaccinationException

class VaccinationNameMissmatchException(
    message: String
) : VaccinationException(
    message = message,
    cause = null
)
