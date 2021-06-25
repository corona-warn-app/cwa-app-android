package de.rki.coronawarnapp.covidcertificate.validationrules.server

import de.rki.coronawarnapp.covidcertificate.validationrules.rules.DgcValidationRule
import de.rki.coronawarnapp.covidcertificate.validationrules.rules.ValidationRuleType
import de.rki.coronawarnapp.covidcertificate.validationrules.server.api.AcceptanceRulesApi
import de.rki.coronawarnapp.covidcertificate.validationrules.server.api.InvalidationRulesApi
import javax.inject.Inject

/**
 * DGC validation rules server
 */
class DgcValidationRulesServer @Inject constructor(
    private val acceptanceRulesApi: AcceptanceRulesApi,
    private val invalidationRulesApi: InvalidationRulesApi,
) {
    suspend fun ruleSet(ruleType: ValidationRuleType): Set<DgcValidationRule> {
        return when (ruleType) {
            ValidationRuleType.ACCEPTANCE -> acceptanceRulesApi.acceptanceRules()
            ValidationRuleType.INVALIDATION -> invalidationRulesApi.invalidationRules()
        }
    }
}
