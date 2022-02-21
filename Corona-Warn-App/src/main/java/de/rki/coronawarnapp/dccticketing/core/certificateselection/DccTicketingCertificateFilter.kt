package de.rki.coronawarnapp.dccticketing.core.certificateselection

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.PCR_TEST
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.RA_TEST
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.RECOVERY
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.TEST
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.VACCINATION
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccTicketingCertificateFilter @Inject constructor(
    private val certificateProvider: CertificateProvider
) {
    suspend fun filter(validationCondition: DccTicketingValidationCondition?): Set<CwaCovidCertificate> {
        val certificateContainer = certificateProvider.certificateContainer.first()
        val vaccinationCerts = certificateContainer.vaccinationCwaCertificates
        val recoveryCerts = certificateContainer.recoveryCwaCertificates
        val testCerts = certificateContainer.testCwaCertificates

        return validationCondition?.type.orEmpty()
            .filterByType(vaccinationCerts, recoveryCerts, testCerts)
            .filterIfExists(validationCondition?.fnt) { cond, cert ->
                cond == cert.rawCertificate.nameData.familyNameStandardized
            }
            .filterIfExists(validationCondition?.gnt) { cond, cert ->
                cond == cert.rawCertificate.nameData.givenNameStandardized
            }
            .filterIfExists(validationCondition?.dob) { cond, cert -> cond == cert.rawCertificate.dob }
    }

    private fun List<String>.filterByType(
        vaccinationCerts: Set<VaccinationCertificate>,
        recoveryCerts: Set<RecoveryCertificate>,
        testCerts: Set<TestCertificate>
    ) = when {
        isEmpty() -> vaccinationCerts + recoveryCerts + testCerts // All certificates should pass
        else -> flatMap { type -> // otherwise filter by types
            when (DccTicketingCertificatesFilterType.typeOf(type)) {
                VACCINATION -> vaccinationCerts
                RECOVERY -> recoveryCerts
                TEST -> testCerts
                PCR_TEST -> testCerts.filter { it.isPCRTestCertificate }
                RA_TEST -> testCerts.filter { it.isRapidAntigenTestCertificate }
                else -> {
                    Timber.tag(TAG).w("Unsupported type=$type")
                    emptySet()
                }
            }
        }
    }.toSet().also {
        Timber.tag(TAG).d("filterByTypeCount=${it.size})")
    }

    private fun Set<CwaCovidCertificate>.filterIfExists(
        condition: String?,
        predicate: (String, CwaCovidCertificate) -> Boolean
    ): Set<CwaCovidCertificate> = when {
        condition != null -> filter { predicate(condition, it) }.toSet()
        else -> this
    }.also {
        Timber.tag(TAG).d("filterIfExists=${it.size}) condition=$condition")
    }

    companion object {
        private val TAG = tag<DccTicketingCertificateFilter>()
    }
}
