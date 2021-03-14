package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.worker.BackgroundConstants
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class BackgroundNoise @Inject constructor(
    private val submissionSettings: SubmissionSettings,
    private val playbook: Playbook
) {
    fun scheduleDummyPattern() {
        if (BackgroundConstants.NUMBER_OF_DAYS_TO_RUN_PLAYBOOK > 0)
            BackgroundWorkScheduler.scheduleBackgroundNoisePeriodicWork()
    }

    suspend fun foregroundScheduleCheck() {
        if (submissionSettings.isAllowedToSubmitKeys) {
            val chance = Random.nextFloat() * 100
            if (chance < DefaultPlaybook.PROBABILITY_TO_EXECUTE_PLAYBOOK_ON_APP_OPEN) {
                playbook.dummy()
            }
        }
    }
}
