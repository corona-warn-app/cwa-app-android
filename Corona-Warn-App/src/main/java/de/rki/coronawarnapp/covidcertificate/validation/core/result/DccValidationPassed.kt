package de.rki.coronawarnapp.covidcertificate.validation.core.result

import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule

data class DccValidationPassed(
    override val expirationCheckPassed: Boolean,
    override val jsonSchemaCheckPassed: Boolean,
    override val acceptanceRules: Set<EvaluatedDccRule>,
    override val invalidationRules: Set<EvaluatedDccRule>
) : DccValidation()
