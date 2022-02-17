package de.rki.coronawarnapp.covidcertificate.common.certificate

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import javax.inject.Inject

@Reusable
class CertificateProvider @Inject constructor(
    vcRepo: VaccinationRepository,
    tcRepo: TestCertificateRepository,
    rcRepo: RecoveryCertificateRepository,
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider
) {

    val certificateContainer: Flow<CertificateContainer> = combine(
        rcRepo.certificates,
        tcRepo.certificates,
        vcRepo.vaccinationInfos
    ) { recoveries, tests, vaccinations -> CertificateContainer(recoveries, tests, vaccinations) }
        .conflate()
        .distinctUntilChanged()
        .shareLatest(scope = appScope + dispatcherProvider.IO)

    /**
     * Finds a [CwaCovidCertificate] by [CertificateContainerId]
     * @throws [Exception] if certificate not found
     */
    suspend fun findCertificate(containerId: CertificateContainerId): CwaCovidCertificate {
        val certificates = certificateContainer.first().allCwaCertificates
        return certificates.find { it.containerId == containerId }!! // Must be a certificate
    }

    data class CertificateContainer(
        val recoveryCertificates: Set<RecoveryCertificateWrapper>,
        val testCertificates: Set<TestCertificateWrapper>,
        val vaccinationInfos: Set<VaccinatedPerson>
    ) {

        val recoveryCwaCertificates: Set<RecoveryCertificate> by lazy {
            recoveryCertificates.map { it.recoveryCertificate }.toSet()
        }

        val testCwaCertificates: Set<TestCertificate> by lazy {
            testCertificates.mapNotNull { it.testCertificate }.toSet()
        }

        val vaccinationCwaCertificates: Set<VaccinationCertificate> by lazy {
            vaccinationInfos.flatMap { it.vaccinationCertificates }.toSet()
        }

        val allCwaCertificates by lazy {
            recoveryCwaCertificates + testCwaCertificates + vaccinationCwaCertificates
        }
    }
}
