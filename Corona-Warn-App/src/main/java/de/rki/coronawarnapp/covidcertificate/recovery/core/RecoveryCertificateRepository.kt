package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val qrCodeExtractor: DccQrCodeExtractor,
    valueSetsRepository: ValueSetsRepository,
) {

    val certificates: Flow<Set<RecoveryCertificateWrapper>> = flowOf(emptySet())

    suspend fun registerCertificate(qrCode: RecoveryCertificateQRCode): RecoveryCertificateContainer {
        Timber.tag(TAG).d("registerCertificate(qrCode=%s)", qrCode)
        throw NotImplementedError()
    }

    suspend fun deleteCertificate(containerId: RecoveryCertificateContainerId): RecoveryCertificateContainer? {
        Timber.tag(TAG).d("deleteCertificate(containerId=%s)", containerId)
        throw NotImplementedError()
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        throw NotImplementedError()
    }

    companion object {
        private val TAG = RecoveryCertificateRepository::class.simpleName!!
    }
}
