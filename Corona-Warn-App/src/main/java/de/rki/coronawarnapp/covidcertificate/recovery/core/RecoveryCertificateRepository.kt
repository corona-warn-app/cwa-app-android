package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorage
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.StoredRecoveryCertificateData
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.last
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

    @Throws(DuplicateRecoveryCertificateException::class)
    suspend fun registerCertificate(qrCode: RecoveryCertificateQRCode): RecoveryCertificateContainer {
        Timber.tag(TAG).d("registerCertificate(qrCode=%s)", qrCode)
        qrCodeExtractor.extract(qrCode.qrCode).toContainer().apply {
            if (internalData.data.last().contains(this)) {
                throw DuplicateRecoveryCertificateException(qrCode.uniqueCertificateIdentifier)
            }
            internalData.updateBlocking { plus(this) }
            return this
        }
    }

    private fun DccQrCode.toContainer() = RecoveryCertificateContainer(
        data = StoredRecoveryCertificateData(
            identifier = uniqueCertificateIdentifier,
            registeredAt = data.header.issuedAt,
            recoveryCertificateQrCode = qrCode
        ),
        qrCodeExtractor = qrCodeExtractor,
        isUpdatingData = false
    )

    suspend fun deleteCertificate(containerId: RecoveryCertificateContainerId) {
        Timber.tag(TAG).d("deleteCertificate(containerId=%s)", containerId)
        internalData.updateBlocking {
            mapNotNull { if (it.containerId == containerId) null else it }.toSet()
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
