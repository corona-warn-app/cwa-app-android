package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule

data class DccValidationData(
    val countries: List<DccCountry>,
    val acceptanceRules: List<DccValidationRule>,
    val invalidationRules: List<DccValidationRule>,
)
