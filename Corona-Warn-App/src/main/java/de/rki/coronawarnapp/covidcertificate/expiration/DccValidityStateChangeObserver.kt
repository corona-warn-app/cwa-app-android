package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Valid
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
    private val dccValidityStateNotificationService: DccValidityStateNotificationService
) {

    fun setup() {
        Timber.tag(TAG).d("setup()")

        certificateProvider.certificateContainer
            .onStart { Timber.tag(TAG).d("Started monitoring certs for state changes") }
            .mapLatest { certificateContainer ->
                certificateContainer.allCwaCertificates
                    .filterNot { it.state is Valid }
                    .associate { it.qrCodeHash to it.state }
            }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .onEach {
                Timber.tag(TAG).d("Dcc validity states: %s", it)
                dccValidityStateNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
            }
            .catch { Timber.tag(TAG).e("Failed to observe certs for state changes") }
            .launchIn(scope = appScope)
    }
}

private val TAG = tag<DccValidityStateChangeObserver>()
