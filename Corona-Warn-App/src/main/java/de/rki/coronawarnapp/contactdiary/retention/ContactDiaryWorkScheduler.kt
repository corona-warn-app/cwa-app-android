package de.rki.coronawarnapp.contactdiary.retention

import androidx.annotation.VisibleForTesting
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ContactDiaryWorkScheduler @Inject constructor(
    @AppScope val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val workBuilder: ContactDiaryWorkBuilder,
    private val onboardingSettings: OnboardingSettings
) : Initializer {

    override fun initialize() {
        onboardingSettings.isOnboardedFlow.onEach { isOnboarded ->
            if (isOnboarded) {
                schedulePeriodic()
            }
        }.launchIn(appScope)
    }

    /**
     * Enqueue background contact diary clean periodic worker
     * Replace with new if older work exists.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun schedulePeriodic() {
        Timber.d("ContactDiaryWorkScheduler schedulePeriodic()")
        // Create unique work and enqueue
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workBuilder.buildPeriodicWork()
        )
    }

    companion object {
        const val PERIODIC_WORK_NAME = "ContactDiaryPeriodicWork"
    }
}
