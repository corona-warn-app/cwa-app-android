package de.rki.coronawarnapp.familytest.worker

import androidx.annotation.VisibleForTesting
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.common.ResultScheduler
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.joda.time.Instant
import org.joda.time.Minutes
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Family test result retrieval
 * every 2 hours in general
 * every 15 min if there are RA tests that are less than 90min registered
 */
@Singleton
class FamilyTestRetrievalScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val repository: FamilyTestRepository,
    private val timeStamper: TimeStamper,
) : ResultScheduler(workManager = workManager) {

    fun setup() {
        repository.familyTestsToRefresh
            .distinctUntilChanged()
            .onEach { tests ->
                adjustPollingSchedule(tests)
            }.launchIn(appScope)
    }

    suspend fun checkPollingSchedule() {
        repository.familyTestsToRefresh.first().let {
            adjustPollingSchedule(it)
        }
    }

    private fun adjustPollingSchedule(tests: Set<FamilyCoronaTest>) = when {
        tests.isEmpty() -> cancelPeriodicWorker()
        tests.any { it.requiresFrequentPolling(timeStamper.nowUTC) } ->
        enqueuePeriodicWorker(FREQUENT_INTERVAL_MINUTES)
        else -> enqueuePeriodicWorker(INTERVAL_MINUTES)
    }

    private fun enqueuePeriodicWorker(repeatIntervalMinutes: Long) {
        Timber.d("enqueueUniquePeriodicWork $PERIODIC_WORK_NAME")
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildPeriodicWork(repeatIntervalMinutes)
        )
    }

    private fun cancelPeriodicWorker() {
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    private fun buildPeriodicWork(repeatIntervalMinutes: Long) =
        PeriodicWorkRequestBuilder<FamilyTestResultRetrievalWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        ).addTag(
            PERIODIC_WORK_NAME
        ).setConstraints(
            Constraints.Builder().apply {
                setRequiredNetworkType(NetworkType.CONNECTED)
            }.build()
        ).setInitialDelay(
            20L,
            TimeUnit.SECONDS
        ).setBackoffCriteria(
            BackoffPolicy.LINEAR,
            BackgroundConstants.KIND_DELAY,
            TimeUnit.MINUTES
        ).build()
}

private const val PERIODIC_WORK_NAME = "FamilyTestResultRetrieval_PeriodicWork"

@VisibleForTesting
internal fun FamilyCoronaTest.requiresFrequentPolling(now: Instant): Boolean {
    return type == BaseCoronaTest.Type.RAPID_ANTIGEN &&
        registeredAt.plus(frequentPollingDuration) > now
}

private val frequentPollingDuration = Minutes.minutes(90).toStandardDuration()
private const val INTERVAL_MINUTES = 120L // every 2h
private const val FREQUENT_INTERVAL_MINUTES = 15L // every 15min
