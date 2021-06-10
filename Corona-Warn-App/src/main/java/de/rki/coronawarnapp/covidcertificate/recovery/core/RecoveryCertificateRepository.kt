package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.RecoveryCertificateQRCodeExtractor
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val qrCodeExtractor: RecoveryCertificateQRCodeExtractor,
    valueSetsRepository: ValueSetsRepository,
) {

    val certificates: Flow<Set<RecoveryCertificateWrapper>> = emptyFlow()

    suspend fun requestCertificate(qrCode: RecoveryCertificateQRCode): RecoveryCertificateContainer {
        Timber.tag(TAG).d("requestCertificate(qrCode=%s)", qrCode)
        throw NotImplementedError()
    }

    suspend fun deleteCertificate(identifier: RecoveryCertificateIdentifier) {
        Timber.tag(TAG).d("deleteTestCertificate(identifier=%s)", identifier)
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
