package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry

data class DccValidationData(
    val countries: List<DccCountry>,
    // TODO add rules
)
