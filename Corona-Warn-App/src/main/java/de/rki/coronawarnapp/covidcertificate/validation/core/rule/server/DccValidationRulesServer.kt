package de.rki.coronawarnapp.covidcertificate.validation.core.rule.server

import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import javax.inject.Inject

/**
 * DCC validation rules server
 */
class DccValidationRulesServer @Inject constructor(
    private val dccValidationRuleApi: DccValidationRuleApi,
) {
    suspend fun ruleSet(ruleTypeDcc: DccValidationRule.Type): Set<DccValidationRule> {
        return when (ruleTypeDcc) {
            DccValidationRule.Type.ACCEPTANCE -> dccValidationRuleApi.acceptanceRules()
            DccValidationRule.Type.INVALIDATION -> dccValidationRuleApi.invalidationRules()
        }
    }
}
