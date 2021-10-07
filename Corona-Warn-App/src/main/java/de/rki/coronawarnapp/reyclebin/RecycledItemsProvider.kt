package de.rki.coronawarnapp.reyclebin

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class RecycledItemsProvider @Inject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
) {

    val recycledCertificates: Flow<Set<CwaCovidCertificate>> = emptyFlow()

    suspend fun restoreCertificate(containerId: CertificateContainerId) {
        when (containerId) {
            is RecoveryCertificateContainerId -> recoveryCertificateRepository.restoreCertificate(containerId)
            is TestCertificateContainerId -> testCertificateRepository.restoreCertificate(containerId)
            is VaccinationCertificateContainerId -> vaccinationRepository.restoreCertificate(containerId)
        }
    }

    suspend fun deleteCertificate(containerId: CertificateContainerId) {
        when (containerId) {
            is RecoveryCertificateContainerId -> recoveryCertificateRepository.deleteCertificate(containerId)
            is TestCertificateContainerId -> testCertificateRepository.deleteCertificate(containerId)
            is VaccinationCertificateContainerId -> vaccinationRepository.deleteCertificate(containerId)
        }
    }

    suspend fun deleteAllCertificate(containerIds: Set<CertificateContainerId>) {
        containerIds.forEach { containerId ->
            deleteCertificate(containerId)
        }
    }
}
