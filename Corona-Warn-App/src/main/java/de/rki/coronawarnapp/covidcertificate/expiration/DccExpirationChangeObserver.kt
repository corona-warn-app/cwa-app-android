package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccExpirationChangeObserver @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val certificateProvider: CertificateProvider,
    private val dccExpirationNotificationService: DccExpirationNotificationService
) {

    fun setup() {
        Timber.tag(TAG).d("setup()")

        certificateProvider.certificateContainer
            .onStart { Timber.tag(TAG).d("Started monitoring certs for state changes") }
            .map { certificateContainer ->
                certificateContainer.allCwaCertificates
                    .filterNot { it.getState() is CwaCovidCertificate.State.Valid }
                    .associate { it.uniqueCertificateIdentifier to it.getState() }
            }
            .distinctUntilChanged()
            .onEach {
                Timber.tag(TAG).d("Expiration changed: %s", it)
                dccExpirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
            }
            .catch { Timber.tag(TAG).e("Failed to observe certs for state changes") }
            .launchIn(scope = appScope)
    }
}

private val TAG = tag<DccExpirationChangeObserver>()
