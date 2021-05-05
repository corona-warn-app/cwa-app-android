package de.rki.coronawarnapp.vaccination.core.server

import de.rki.coronawarnapp.vaccination.core.VaccinationException

open class ProofCertificateException(
    cause: Throwable?,
    message: String
) : VaccinationException(
    message = message,
    cause = cause
)
