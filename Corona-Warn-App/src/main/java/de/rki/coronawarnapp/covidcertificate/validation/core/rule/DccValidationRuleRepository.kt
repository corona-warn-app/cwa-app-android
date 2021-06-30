package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.server.DccValidationRulesServer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccValidationRuleRepository @Inject constructor(
    dccValidationRulesServer: DccValidationRulesServer
) {

    suspend fun acceptanceRules(arrivalCountry: DccCountry): List<DccValidationRule> {
        return emptyList() // TODO
    }

    suspend fun invalidationRules(arrivalCountry: DccCountry): List<DccValidationRule> {
        return emptyList() // TODO
    }
}
