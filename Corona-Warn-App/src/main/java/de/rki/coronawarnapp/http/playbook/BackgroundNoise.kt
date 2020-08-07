package de.rki.coronawarnapp.http.playbook

import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.storage.LocalData
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

    fun scheduleDummyPattern() {
        BackgroundWorkScheduler.scheduleBackgroundNoisePeriodicWork()
    }

    suspend fun foregroundScheduleCheck() {
        if (LocalData.isAllowedToSubmitDiagnosisKeys() == true) {
            val chance = Random.nextFloat() * 100
            if (chance < SubmissionConstants.probabilityToExecutePlaybookWhenOpenApp) {
                PlaybookImpl(WebRequestBuilder.getInstance())
                    .dummy()
            }
        }
    }
}
