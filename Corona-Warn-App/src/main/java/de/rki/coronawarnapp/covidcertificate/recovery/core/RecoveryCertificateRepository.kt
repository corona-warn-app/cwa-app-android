package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidRecoveryCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorage
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.StoredRecoveryCertificateData
import de.rki.coronawarnapp.covidcertificate.signature.core.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    private val qrCodeExtractor: DccQrCodeExtractor,
    valueSetsRepository: ValueSetsRepository,
    private val storage: RecoveryCertificateStorage,
    private val dccStateChecker: DccStateChecker,
) {

    private val internalData: HotDataFlow<Set<RecoveryCertificateContainer>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        storage.load()
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
            .onStart { Timber.tag(TAG).d("Observing RecoveryCertificateContainer data.") }
            .drop(1) // Initial emission, restored from storage.
            .onEach { recoveryCertificates ->
                Timber.tag(TAG).v("Recovery Certificate data changed: %s", recoveryCertificates)
                storage.save(recoveryCertificates.map { it.data }.toSet())
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot recovery certificate data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    val certificates: Flow<Set<RecoveryCertificateWrapper>> = internalData.data
        .map { set ->
            set.map { container ->
                val state = dccStateChecker.checkState(container.certificateData).first()
                RecoveryCertificateWrapper(
                    valueSets = valueSetsRepository.latestVaccinationValueSets.first(),
                    container = container,
                    certificateState = state
                )
            }.toSet()
        }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    @Throws(InvalidRecoveryCertificateException::class)
    suspend fun registerCertificate(qrCode: RecoveryCertificateQRCode): RecoveryCertificateContainer {
        Timber.tag(TAG).d("registerCertificate(qrCode=%s)", qrCode)
        val newContainer = qrCode.toContainer()
        internalData.updateBlocking {
            if (any { it.certificateId == newContainer.certificateId }) {
                throw InvalidRecoveryCertificateException(
                    InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
                )
            }
            plus(newContainer)
        }
        return newContainer
    }

    private fun RecoveryCertificateQRCode.toContainer() = RecoveryCertificateContainer(
        data = StoredRecoveryCertificateData(
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

    suspend fun setNotifiedState(
        containerId: RecoveryCertificateContainerId,
        state: CwaCovidCertificate.State,
        time: Instant?,
    ) {
        Timber.tag(TAG).d("setNotifiedAboutState(containerId=$containerId, time=$time)")
        internalData.updateBlocking {
            val toUpdate = singleOrNull { it.containerId == containerId }
            if (toUpdate == null) {
                Timber.tag(TAG).w("Couldn't find %s", containerId)
                return@updateBlocking this
            }

            val newData = when (state) {
                is CwaCovidCertificate.State.Expired -> toUpdate.data.copy(notifiedExpiredAt = time)
                is CwaCovidCertificate.State.ExpiringSoon -> toUpdate.data.copy(notifiedExpiresSoonAt = time)
                else -> throw  UnsupportedOperationException("$state is not supported.")
            }

            this.minus(toUpdate).plus(
                toUpdate.copy(data = newData).also {
                    Timber.tag(TAG).d("Updated %s", it)
                }
            )
        }
    }

    companion object {
        private val TAG = RecoveryCertificateRepository::class.simpleName!!
    }
}
