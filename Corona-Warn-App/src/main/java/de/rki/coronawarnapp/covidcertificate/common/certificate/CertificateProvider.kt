package de.rki.coronawarnapp.covidcertificate.common.certificate

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
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

    val allCertificates: Flow<Set<CwaCovidCertificate>> = combine(
        vcRepo.cwaCertificates,
        tcRepo.cwaCertificates,
        rcRepo.cwaCertificates
    ) { vaccinations, tests, recoveries -> (vaccinations + tests + recoveries) }
        .conflate()
        .distinctUntilChanged()
        .shareLatest(scope = appScope + dispatcherProvider.IO)

    /**
     * Finds a [CwaCovidCertificate] by [CertificateContainerId]
     * @throws [Exception] if certificate not found
     */
    suspend fun findCertificate(containerId: CertificateContainerId): CwaCovidCertificate {
        val certificates = allCertificates.first()
        return certificates.find { it.containerId == containerId }!! // Must be a certificate
    }
}
