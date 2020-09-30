package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.worker.BackgroundConstants
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlin.random.Random

class BackgroundNoise {
    companion object {
        @Volatile
        private var instance: BackgroundNoise? = null

        fun getInstance(): BackgroundNoise {
            return instance ?: synchronized(this) {
                instance ?: BackgroundNoise().also {
                    instance = it
                }
            }
        }
    }

    private val playbook: Playbook
        get() = AppInjector.component.playbook

    fun scheduleDummyPattern() {
        if (BackgroundConstants.NUMBER_OF_DAYS_TO_RUN_PLAYBOOK > 0)
            BackgroundWorkScheduler.scheduleBackgroundNoisePeriodicWork()
    }

    suspend fun foregroundScheduleCheck() {
        if (LocalData.isAllowedToSubmitDiagnosisKeys() == true) {
            val chance = Random.nextFloat() * 100
            if (chance < DefaultPlaybook.PROBABILITY_TO_EXECUTE_PLAYBOOK_ON_APP_OPEN) {
                playbook.dummy()
            }
        }
    }
}
