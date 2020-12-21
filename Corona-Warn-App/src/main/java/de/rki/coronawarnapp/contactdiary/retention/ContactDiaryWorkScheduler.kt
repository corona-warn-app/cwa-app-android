package de.rki.coronawarnapp.contactdiary.retention

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ContactDiaryWorkScheduler @Inject constructor(
    val workManager: WorkManager,
    private val workBuilder: ContactDiaryWorkBuilder
) {

    /**
     * Enqueue background contact diary clean periodic worker
     * Replace with new if older work exists.
     */
    fun schedulePeriodic() {
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
