package de.rki.coronawarnapp.covidcertificate.validation.core.business

import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule

data class BusinessValidation(
    val acceptanceRules: Set<EvaluatedDccRule>,
    val invalidationRules: Set<EvaluatedDccRule>
)
