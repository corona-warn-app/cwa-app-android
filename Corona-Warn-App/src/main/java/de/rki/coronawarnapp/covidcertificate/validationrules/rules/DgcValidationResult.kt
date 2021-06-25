package de.rki.coronawarnapp.covidcertificate.validationrules.rules

interface DgcValidationResult {
    val expirationCheckPassed: Boolean
    val jsonSchemaCheckPassed: Boolean
    val acceptanceRulesResultDgcs: Set<DgcValidationResultSet>
    val invalidationRulesResultDgcs: Set<DgcValidationResultSet>
}
