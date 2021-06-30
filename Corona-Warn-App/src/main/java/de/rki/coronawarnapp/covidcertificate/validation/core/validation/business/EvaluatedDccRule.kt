package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule

data class EvaluatedDccRule(
    val rule: DccValidationRule,
    val result: DccValidationRule.Result,
)
