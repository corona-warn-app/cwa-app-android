package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorage
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    private val qrCodeExtractor: DccQrCodeExtractor,
    private val storage: RecoveryCertificateStorage,
) {

    private val internalData: HotDataFlow<Set<RecoveryCertificateContainer>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        storage.recoveryCertificates
            .map { recoveryCertificate ->
                RecoveryCertificateContainer(
                    data = recoveryCertificate,
                    qrCodeExtractor = qrCodeExtractor
                )
            }
            .toSet()
            .also { Timber.tag(TAG).v("Restored recovery certificate data: %s", it) }
    }

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing data.") }
            .onEach { recoveryCertificates ->
                Timber.tag(TAG).v("Recovery Certificate data changed: %s", recoveryCertificates)
                storage.recoveryCertificates = recoveryCertificates.map { it.data }.toSet()
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot recovery certificate data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    val certificates: Flow<Set<RecoveryCertificateWrapper>> =
        internalData.data.transform { set ->
            set.map { RecoveryCertificateWrapper(null, it) }.toSet()
        }

    suspend fun requestCertificate(qrCode: RecoveryCertificateQRCode): RecoveryCertificateContainer {
        Timber.tag(TAG).d("requestCertificate(qrCode=%s)", qrCode)
        internalData.updateBlocking {
            this.plus(
                RecoveryCertificateContainer(
                data = , // TODO convert qr code to entity
                qrCodeExtractor = qrCodeExtractor,
                isUpdatingData = false
            ))
        }
    }

    suspend fun deleteCertificate(identifier: RecoveryCertificateIdentifier) {
        Timber.tag(TAG).d("deleteCertificate(identifier=%s)", identifier)
        internalData.updateBlocking {
            mapNotNull { if (it.data.identifier == identifier) null else it }.toSet()
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).w("Clearing recovery certificate data.")
        internalData.updateBlocking {
            Timber.tag(TAG).v("Deleting: %s", this)
            emptySet()
        }
    }

    companion object {
        private val TAG = RecoveryCertificateRepository::class.simpleName!!
    }
}
