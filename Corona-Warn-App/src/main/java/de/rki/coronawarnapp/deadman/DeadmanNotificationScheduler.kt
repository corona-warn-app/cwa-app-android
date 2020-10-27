package de.rki.coronawarnapp.deadman

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dagger.Reusable
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.deadman.DeadmanNotificationTimeCalculation.Companion.DEADMAN_NOTIFICATION_DELAY
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.flow.first
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.Hours
import org.joda.time.Instant
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DeadmanNotificationScheduler @Inject constructor() {

    /**
     * Work manager instance
     */
    private val workManager by lazy { WorkManager.getInstance(CoronaWarnApplication.getAppContext()) }

    /**
     * Enqueue background deadman notification onetime work
     * Replace with new if older work exists.
     */
    suspend fun scheduleOneTime() {
        // Get initial delay
        val delay = DeadmanNotificationTimeCalculation().getDelay()

//        Timber.d("Delay: $delay")

        // TODO: seperate logic?
        if(delay < 0) {
            return // TODO: <- Dont like this one
        } else {
            //Create unique work and enqueue
            workManager.enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                DeadmanNotificationWorkBuilder().buildOneTimeWork(delay)
            )
        }
    }

    /**
     * Enqueue background deadman notification onetime work
     * Replace with new if older work exists.
     */
    fun schedulePeriodic() {
        //Create unique work and enqueue
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            DeadmanNotificationWorkBuilder().buildPeriodicWork()
        )
    }

    companion object {
        /**
         * Deadman notification one time work
         */
        const val ONE_TIME_WORK_NAME = "DeadmanNotificationOneTimeWork";
        /**
         * Deadman notification periodic work
         */
        const val PERIODIC_WORK_NAME = "DeadmanNotificationPeriodicWork";
    }
}
