package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.ccl.configuration.update.CclConfigurationUpdater
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Valid
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccValidityStateChangeObserver @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val certificateProvider: CertificateProvider,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val vaccinationCertificateRepository: VaccinationCertificateRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val dccValidityStateNotificationService: DccValidityStateNotificationService,
) : Initializer {

    override fun initialize() {
        Timber.tag(TAG).d("setup()")

        certificateProvider.certificateContainer
            .onStart { Timber.tag(TAG).d("Started monitoring certs for state changes") }
            .mapLatest { certificateContainer ->
                certificateContainer.allCwaCertificates
                    .onEach {
                        when (it.containerId) {
                            is RecoveryCertificateContainerId -> {
                                recoveryCertificateRepository
                                    .acknowledgeState(it.containerId as RecoveryCertificateContainerId)
                            }
                            is VaccinationCertificateContainerId -> {
                                vaccinationCertificateRepository
                                    .acknowledgeState(it.containerId as VaccinationCertificateContainerId)
                            }
                            is TestCertificateContainerId -> {
                                testCertificateRepository
                                    .acknowledgeState(it.containerId as TestCertificateContainerId)
                            }
                        }
                    }
                    .filterNot { it.state is Valid }
                    .associate { it.qrCodeHash to it.state }
            }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .onEach {
                Timber.tag(TAG).d("Dcc validity states: %s", it)
                dccValidityStateNotificationService.showNotificationIfStateChanged(forceCheck = true)
            }
            .catch { Timber.tag(TAG).e("Failed to observe certs for state changes") }
            .launchIn(scope = appScope)
    }
}

private val TAG = tag<DccValidityStateChangeObserver>()
