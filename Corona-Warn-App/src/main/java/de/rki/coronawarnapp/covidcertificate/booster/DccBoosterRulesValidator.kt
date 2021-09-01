package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccBoosterRulesValidator @Inject constructor() {
    suspend fun validateBoosterRules(
        dccList: List<CwaCovidCertificate>,
        boosterRules: List<DccValidationRule>
    ): DccValidationRule? {
        // TODO
        throw NotImplementedError()
    }
}
