package de.rki.coronawarnapp.covidcertificate.revocation.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevocationListUpdater @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val timeStamper: TimeStamper,
    private val certificatesProvider: CertificateProvider,
    private val revocationUpdateSettings: RevocationUpdateSettings,
    private val revocationRepository: RevocationRepository,
) {

    private val mutex = Mutex()

    init {
        combine(
            certificatesProvider.certificateContainer,
            certificatesProvider.recycledCertificateContainer
        ) { certificateContainer, recycledCertificateContainer ->
            certificateContainer.allCwaCertificates + recycledCertificateContainer.allCwaCertificates
        }.drop(1) // App start emission
            .distinctUntilChanged { old, new ->
                // Compare all certificates size in App recycled or not, so only new added certificates would count
                // moving to and from recycle bin is not a new registration
                old.size < new.size
            }.onEach {
                updateRevocationList(true) // Force updating the list
            }.catch {
                Timber.tag(TAG).d("Updating revocation list failed on new certificate addition -> %s", it.message)
            }.launchIn(appScope)
    }

    suspend fun updateRevocationList(forceUpdate: Boolean = false) = mutex.withLock {
        val updateRequired = isUpdateRequired()
        Timber.tag(TAG).d("updateRevocationList(forceUpdate=$forceUpdate, updateRequired=$updateRequired)")
        if (forceUpdate || updateRequired) {
            Timber.tag(TAG).d("updateRevocationList is required")
            revocationRepository.updateRevocationList(
                certificatesProvider.certificateContainer.first().allCwaCertificates
            )
        } else {
            Timber.tag(TAG).d("updateRevocationList isn't required")
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun isUpdateRequired(now: Instant = timeStamper.nowUTC): Boolean {
        val lastExecution = revocationUpdateSettings.getLastUpdateTime() ?: return true

        // update is needed if the last update was on a different day
        return lastExecution.toLocalDateUtc() != now.toLocalDateUtc()
    }

    companion object {
        private val TAG = tag<RevocationListUpdater>()
    }
}
