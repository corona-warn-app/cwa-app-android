package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccBoosterRulesValidator @Inject constructor(
    private val boosterRulesRepository: BoosterRulesRepository
) {
    suspend fun validateBoosterRules(dccList: List<CwaCovidCertificate>): EvaluatedDccRule? {
        Timber.tag(TAG).d("validateBoosterRules(dccList=%s)", dccList)

        val boosterRules = boosterRulesRepository.rules.first()
        Timber.tag(TAG).d("boosterRules=%s", boosterRules)

        // Find recent vaccination certificate
        val vacCertificates = dccList.filterIsInstance<VaccinationCertificate>()
        val recentVaccinatedOnCert = vacCertificates.maxByOrNull { it.vaccinatedOn }
        val recentVaccinationCertificate = vacCertificates
            .filter { it.vaccinatedOn == recentVaccinatedOnCert?.vaccinatedOn }
            .maxByOrNull { it.headerIssuedAt }

        if (recentVaccinationCertificate == null) {
            Timber.tag(TAG).d("No vaccination certificate found")
            return null
        }
        Timber.tag(TAG).d("Most recent vaccination certificate=%s", recentVaccinationCertificate)

        // Find recent recovery certificate
        val recCertificates = dccList.filterIsInstance<RecoveryCertificate>()
        val recentFirstResultOnCert = recCertificates.maxByOrNull { it.testedPositiveOn }
        val recentRecoveryCertificate = recCertificates
            .filter { it.testedPositiveOn == recentFirstResultOnCert?.testedPositiveOn }
            .maxByOrNull { it.headerIssuedAt }
        Timber.tag(TAG).d("Most recent recovery certificate=%s", recentRecoveryCertificate)


        return null
    }

    companion object {
        private val TAG = DccBoosterRulesValidator::class.simpleName
    }
}
