package de.rki.coronawarnapp.deadman

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DeadmanNotificationScheduler @Inject constructor(
    @AppScope val appScope: CoroutineScope,
    val timeCalculation: DeadmanNotificationTimeCalculation,
    val workManager: WorkManager,
    val workBuilder: DeadmanNotificationWorkBuilder,
    val onboardingSettings: OnboardingSettings,
    val enfClient: ENFClient
) {

    fun setup() {
        Timber.i("setup() DeadmanNotificationScheduler")

        combine(
            onboardingSettings.isOnboardedFlow,
            enfClient.isTracingEnabled
        ) { isOnboarded, isTracingEnabled ->
            Timber.d(
                "isOnboarded = $isOnboarded, " +
                    "isTracingEnabled = $isTracingEnabled"
            )
            isOnboarded && isTracingEnabled
        }.onEach { shouldSchedulePeriodic ->
            Timber.d("shouldSchedulePeriodic: $shouldSchedulePeriodic")
            if (shouldSchedulePeriodic) {
                schedulePeriodic()
            } else {
                cancelScheduledWork()
            }
        }.launchIn(appScope)
    }

    /**
     * Enqueue background deadman notification onetime work
     * Replace with new if older work exists.
     */
    suspend fun scheduleOneTime() {
        // Get initial delay
        val delay = timeCalculation.getDelayInMinutes()

        if (delay < 0) {
            return
        } else {
            Timber.d("DeadmanNotification will be scheduled for $delay minutes in the future")
            // Create unique work and enqueue
            workManager.enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workBuilder.buildOneTimeWork(delay)
            )
        }
    }

    /**
     * Enqueue background deadman notification periodic work
     * Do -NOT- Replace with new if older work exists.
     * As this will result in the notification spam seen in the past
     * because running a new check every time the app is opened
     * spawns a notification every time the last check
     * was more than 35 Hours ago.
     */
    fun schedulePeriodic() {
        // Create unique work and enqueue
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workBuilder.buildPeriodicWork()
        )
    }

    fun cancelScheduledWork() {
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
        workManager.cancelUniqueWork(ONE_TIME_WORK_NAME)
        Timber.d("DeadmanNotification Jobs Cancelled")
    }

    companion object {
        /**
         * Deadman notification one time work
         */
        const val ONE_TIME_WORK_NAME = "DeadmanNotificationOneTimeWork"

        /**
         * Deadman notification periodic work
         */
        const val PERIODIC_WORK_NAME = "DeadmanNotificationPeriodicWork"
    }
}
