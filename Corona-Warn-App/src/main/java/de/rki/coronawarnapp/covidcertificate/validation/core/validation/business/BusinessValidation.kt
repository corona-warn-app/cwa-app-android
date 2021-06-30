package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

interface BusinessValidation {
    val acceptanceRules: Set<EvaluatedDccRule>
    val invalidationRules: Set<EvaluatedDccRule>
}
