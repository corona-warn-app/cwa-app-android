package de.rki.coronawarnapp.covidcertificate.common.certificate

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Reusable
class CertificateProvider @Inject constructor(
    private val vcRepo: VaccinationRepository,
    private val tcRepo: TestCertificateRepository,
    private val rcRepo: RecoveryCertificateRepository,
) {

    /**
     * Finds a [CwaCovidCertificate] by [CertificateContainerId]
     * @throws [Exception] if certificate not found
     */
    suspend fun findCertificate(containerId: CertificateContainerId): CwaCovidCertificate {
        val certificates = rcRepo.certificates.first().mapNotNull { it.recoveryCertificate } +
            tcRepo.certificates.first().mapNotNull { it.testCertificate } +
            vcRepo.vaccinationInfos.first().flatMap { person -> person.vaccinationCertificates }

        return certificates.find { it.containerId == containerId }!! // Must be a certificate
    }
}
