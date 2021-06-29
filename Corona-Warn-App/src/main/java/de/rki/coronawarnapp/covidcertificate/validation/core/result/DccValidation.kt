package de.rki.coronawarnapp.covidcertificate.validation.core.result

import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule

sealed class DccValidation {
    abstract val expirationCheckPassed: Boolean
    abstract val jsonSchemaCheckPassed: Boolean
    abstract val acceptanceRules: Set<EvaluatedDccRule>
    abstract val invalidationRules: Set<EvaluatedDccRule>
}
