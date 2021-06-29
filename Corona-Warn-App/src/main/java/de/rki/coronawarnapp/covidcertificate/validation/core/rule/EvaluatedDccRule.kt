package de.rki.coronawarnapp.covidcertificate.validation.core.rule

data class EvaluatedDccRule(
    val rule: DccValidationRule,
    val result: DccValidationRule.Result,
)
