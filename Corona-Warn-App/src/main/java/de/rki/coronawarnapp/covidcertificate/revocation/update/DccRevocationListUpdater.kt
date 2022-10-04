package de.rki.coronawarnapp.covidcertificate.revocation.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.launchInLatest
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccRevocationListUpdater @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val timeStamper: TimeStamper,
    private val certificatesProvider: CertificateProvider,
    private val revocationUpdateSettings: DccRevocationUpdateSettings,
    private val revocationUpdateService: DccRevocationUpdateService
) {

    private val mutex = Mutex()

    init {
        certificatesProvider.allCertificates
            .distinctUntilChangedBy { it.size }
            .drop(1)
            .catch { Timber.tag(TAG).e(it, "An error occurred while observing all certificates") }
            .launchInLatest(appScope) {
                Timber.tag(TAG).d("Update revocation list on new registration")
                updateRevocationList(forceUpdate = true, allCertificates = it)
            }
    }

    suspend fun updateRevocationList(
        forceUpdate: Boolean = false,
        allCertificates: Set<CwaCovidCertificate>? = null
    ) = mutex.withLock {
        try {
            val timeUpdateRequired = isUpdateRequired()
            Timber.tag(TAG).d("updateRevocationList(forceUpdate=$forceUpdate, timeUpdateRequired=$timeUpdateRequired)")
            when {
                forceUpdate || timeUpdateRequired -> {
                    Timber.tag(TAG).d("updateRevocationList is required")
                    revocationUpdateService.updateRevocationList(
                        allCertificates = allCertificates ?: certificatesProvider.allCertificates.first()
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
        private val TAG = tag<DccRevocationListUpdater>()
    }
}
