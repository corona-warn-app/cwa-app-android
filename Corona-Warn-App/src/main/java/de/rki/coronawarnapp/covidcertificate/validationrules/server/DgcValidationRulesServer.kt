package de.rki.coronawarnapp.covidcertificate.validationrules.server

import de.rki.coronawarnapp.covidcertificate.validationrules.rules.DgcValidationRule
import de.rki.coronawarnapp.covidcertificate.validationrules.rules.DgcValidationRuleType
import de.rki.coronawarnapp.covidcertificate.validationrules.server.api.DgcAcceptanceRulesApi
import de.rki.coronawarnapp.covidcertificate.validationrules.server.api.DgcInvalidationRulesApi
import javax.inject.Inject

/**
 * DGC validation rules server
 */
class DgcValidationRulesServer @Inject constructor(
    private val dgcAcceptanceRulesApi: DgcAcceptanceRulesApi,
    private val dgcInvalidationRulesApi: DgcInvalidationRulesApi,
) {
    suspend fun ruleSet(ruleTypeDgc: DgcValidationRuleType): Set<DgcValidationRule> {
        return when (ruleTypeDgc) {
            DgcValidationRuleType.ACCEPTANCE -> dgcAcceptanceRulesApi.acceptanceRules()
            DgcValidationRuleType.INVALIDATION -> dgcInvalidationRulesApi.invalidationRules()
        }
    }
}
