package de.rki.coronawarnapp.covidcertificate.validationrules.rules

interface DgcValidationResultSet {
    val rule: DgcValidationRule
    val result: DgcValidationResultType
}
