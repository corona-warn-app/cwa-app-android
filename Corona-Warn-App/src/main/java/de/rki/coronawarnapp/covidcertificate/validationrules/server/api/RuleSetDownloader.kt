package de.rki.coronawarnapp.covidcertificate.validationrules.server.api

import de.rki.coronawarnapp.covidcertificate.validationrules.rules.DgcValidationRule
import de.rki.coronawarnapp.covidcertificate.validationrules.rules.ValidationRuleType

interface RuleSetDownloader {
    suspend fun downloadRuleSet(validationRuleType: ValidationRuleType): Set<DgcValidationRule>
}
