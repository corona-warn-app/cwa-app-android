package de.rki.coronawarnapp.reyclebin

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class RecycledItemsProvider @Inject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
) {

    val recycledCertificates: Flow<Set<CwaCovidCertificate>> = combine(
        vaccinationRepository.recycledCertificates,
        testCertificateRepository.recycledCertificates,
        recoveryCertificateRepository.recycledCertificates
    ) { recycledVacCerts, recycledTestCerts, recycledRecCerts ->
        recycledVacCerts.plus(recycledTestCerts).plus(recycledRecCerts).also {
            Timber.tag(TAG).d("recycledCertificates=%s", it)
        }
    }

    /**
     * Find certificate in recycled items
     * @return [CertificateContainerId] if found , otherwise `null`
     */
    suspend fun findCertificate(dccRawQrCode: String): CertificateContainerId? {
        Timber.tag(TAG).d("findCertificate()")
        return recycledCertificates.first().find { it.qrCodeToDisplay.content == dccRawQrCode }?.containerId
    }

    suspend fun restoreCertificate(containerId: CertificateContainerId) {
        Timber.tag(TAG).d("restoreCertificate(containerId=%s)", containerId)
        when (containerId) {
            is RecoveryCertificateContainerId -> recoveryCertificateRepository.restoreCertificate(containerId)
            is TestCertificateContainerId -> testCertificateRepository.restoreCertificate(containerId)
            is VaccinationCertificateContainerId -> vaccinationRepository.restoreCertificate(containerId)
        }
    }

    suspend fun deleteCertificate(containerId: CertificateContainerId) {
        Timber.tag(TAG).d("deleteCertificate(containerId=%s)", containerId)
        when (containerId) {
            is RecoveryCertificateContainerId -> recoveryCertificateRepository.deleteCertificate(containerId)
            is TestCertificateContainerId -> testCertificateRepository.deleteCertificate(containerId)
            is VaccinationCertificateContainerId -> vaccinationRepository.deleteCertificate(containerId)
        }
    }

    suspend fun deleteAllCertificate(containerIds: Set<CertificateContainerId>) {
        Timber.tag(TAG).d("deleteAllCertificate(containerIds=%s)", containerIds)
        containerIds.forEach { containerId ->
            deleteCertificate(containerId)
        }
    }

    companion object {
        val TAG = tag<RecycledItemsProvider>()
    }
}
