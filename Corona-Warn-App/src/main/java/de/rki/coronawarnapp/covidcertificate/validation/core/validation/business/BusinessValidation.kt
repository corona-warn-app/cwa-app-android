package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

import de.rki.coronawarnapp.covidcertificate.validation.core.validation.EvaluatedDccRule

interface BusinessValidation {
    val acceptanceRules: Set<EvaluatedDccRule>
    val invalidationRules: Set<EvaluatedDccRule>
}
