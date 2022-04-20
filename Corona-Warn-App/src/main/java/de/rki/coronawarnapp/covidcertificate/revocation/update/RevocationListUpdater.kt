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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
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
    private val revocationUpdateService: RevocationUpdateService
) {

    private val mutex = Mutex()

    init {
        appScope.launch {
            certificatesProvider.allCertificatesSize
                .drop(1) // App start emission
                .collectLatest {
                    Timber.tag(TAG).d("Update revocation list on new registration")
                    updateRevocationList(true)
                }
        }
    }

    suspend fun updateRevocationList(forceUpdate: Boolean = false) = mutex.withLock {
        try {
            val timeUpdateRequired = isUpdateRequired()
            Timber.tag(TAG).d("updateRevocationList(forceUpdate=$forceUpdate, timeUpdateRequired=$timeUpdateRequired)")
            when {
                forceUpdate || timeUpdateRequired -> {
                    Timber.tag(TAG).d("updateRevocationList is required")
                    revocationUpdateService.updateRevocationList(
                        certificatesProvider.certificateContainer.first()
                    )
                    revocationUpdateSettings.setUpdateTimeToNow()
                }
                else -> Timber.tag(TAG).d("updateRevocationList isn't required")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).d("updateRevocationList failed ->%s", e.message)
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
