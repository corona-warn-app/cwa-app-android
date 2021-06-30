package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountryRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccValidationRepository @Inject constructor(
    private val dccCountryRepository: DccCountryRepository,
    private val validationRulesRepository: DccValidationRuleRepository,
) {

    /**
     * The UI calls this before entering the validation flow.
     * Either we have a cached valid data to work with, or this throws an error for the UI to display.
     */
    suspend fun refresh() {
        dccCountryRepository.refresh()
        validationRulesRepository.refresh()
    }

    suspend fun clear() {
        dccCountryRepository.clear()
        validationRulesRepository.clear()
    }
}
