package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCodeExtractor
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorage
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val qrCodeExtractor: RecoveryCertificateQRCodeExtractor,
    valueSetsRepository: ValueSetsRepository,
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
            .onStart { Timber.tag(TAG).d("Observing test data.") }
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

    val certificates: Flow<Set<RecoveryCertificateWrapper>> = combine(
        internalData.data,
        valueSetsRepository.latestVaccinationValueSets // TODO use corrects sets
    ) { containers, currentValueSet ->
        containers.map { RecoveryCertificateWrapper(currentValueSet, it) }.toSet()
    }

    suspend fun requestCertificate(qrCode: RecoveryCertificateQRCode): RecoveryCertificateContainer {
        Timber.tag(TAG).d("requestCertificate(qrCode=%s)", qrCode)
        throw NotImplementedError()
    }

    suspend fun deleteCertificate(identifier: RecoveryCertificateIdentifier) {
        Timber.tag(TAG).d("deleteCertificate(identifier=%s)", identifier)
        internalData.updateBlocking {
            mapNotNull { if (it.certificateId == identifier) null else it }.toSet()
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
