package de.rki.coronawarnapp.covidcertificate.revocation.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        appScope.launch {
            certificatesProvider.certificateContainer.map {

            }.distinctUntilChanged()
                .collectLatest {
                    updateRevocationList()
                }
        }
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
