package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule

data class DccValidation(
    val expirationCheckPassed: Boolean,
    val jsonSchemaCheckPassed: Boolean,
    val acceptanceRules: Set<EvaluatedDccRule>,
    val invalidationRules: Set<EvaluatedDccRule>,
) {

    val state: State
        get() = State.PASSED // TODO

    enum class State {
        PASSED,
        OPEN,
        TECHNICAL_FAILURE,
        FAILURE,
    }
}
