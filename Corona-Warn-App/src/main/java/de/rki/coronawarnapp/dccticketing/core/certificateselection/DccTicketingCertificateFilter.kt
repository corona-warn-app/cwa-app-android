package de.rki.coronawarnapp.dccticketing.core.certificateselection

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.core.toCertificateSortOrder
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccTicketingCertificateFilter @Inject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
) {
    suspend fun filter(dccTicketingAccessToken: DccTicketingAccessToken): Set<CwaCovidCertificate> {
        val validationCondition = dccTicketingAccessToken.vc

        val vaccinationCerts = vaccinationRepository.recycledCertificates.first()
        val recoveryCerts = recoveryCertificateRepository.recycledCertificates.first()
        val testCerts = testCertificateRepository.recycledCertificates.first()

        val types = validationCondition?.type.orEmpty().let { ts ->
            if (ts.isEmpty()) FilterType.values().map { it.type } else ts
        }

        return types
            .filterByType(vaccinationCerts, recoveryCerts, testCerts)
            .filterIfExists(validationCondition?.fnt) { cond, cert -> cond == cert.rawCertificate.nameData.familyName }
            .filterIfExists(validationCondition?.gnt) { cond, cert -> cond == cert.rawCertificate.nameData.givenName }
            .filterIfExists(validationCondition?.dob) { cond, cert -> cond == cert.rawCertificate.dob }
            .toCertificateSortOrder()
            .toSet()
    }

    private fun List<String>.filterByType(
        vaccinationCerts: Set<VaccinationCertificate>,
        recoveryCerts: Set<RecoveryCertificate>,
        testCerts: Set<TestCertificate>
    ) = flatMap { type ->
        when (FilterType.typeOf(type)) {
            FilterType.VACCINATION -> vaccinationCerts
            FilterType.RECOVERY -> recoveryCerts
            FilterType.TEST -> testCerts
            FilterType.PCR_TEST -> testCerts.filter { it.rawCertificate.test.testType == "LP6464-4" }
            FilterType.RA_TEST -> testCerts.filter { it.rawCertificate.test.testType == "LP217198-3" }
            else -> {
                Timber.tag(TAG).w("Unsupported type=$type")
                emptySet()
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

    private enum class FilterType(val type: String) {
        VACCINATION("v"),
        RECOVERY("r"),
        TEST("t"),
        PCR_TEST("tp"),
        RA_TEST("tr");

        companion object {
            fun typeOf(type: String): FilterType? = values().find { it.type == type }
        }
    }

    companion object {
        private val TAG = tag<DccTicketingCertificateFilter>()
    }
}
