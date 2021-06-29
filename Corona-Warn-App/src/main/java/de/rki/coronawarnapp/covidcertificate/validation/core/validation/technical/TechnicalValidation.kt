package de.rki.coronawarnapp.covidcertificate.validation.core.validation.technical

interface TechnicalValidation {
    val expirationCheckPassed: Boolean
    val jsonSchemaCheckPassed: Boolean
}
