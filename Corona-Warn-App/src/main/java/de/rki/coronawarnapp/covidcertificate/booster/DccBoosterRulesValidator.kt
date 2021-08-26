package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccBoosterRulesValidator @Inject constructor(
    private val boosterRulesRepository: BoosterRulesRepository
) {
    suspend fun validateBoosterRules(dccList: List<DccData<*>>): EvaluatedDccRule? {
        // TODO
        throw NotImplementedError()
    }
}
