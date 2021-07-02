package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.server.DccValidationRulesServer
import timber.log.Timber
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

    suspend fun refresh(): List<DccValidationRule> {
        // TODO blocking method that refreshes current rule data and retrows errors, for the UI to call
        return emptyList()
    }

    suspend fun clear() {
        Timber.i("clear()")
        // TODO
    }
}
