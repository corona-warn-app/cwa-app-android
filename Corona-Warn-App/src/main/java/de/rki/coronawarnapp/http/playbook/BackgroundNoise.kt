package de.rki.coronawarnapp.http.playbook

import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.storage.LocalData
import kotlinx.coroutines.delay
import kotlin.random.Random

object BackgroundNoise {
    private val playbook: Playbook = PlaybookImpl(WebRequestBuilder.getInstance())

    fun scheduleDummyPattern() {
        // The mobile application must implement a frequent schedule to repeat this communication pattern for a period of at least 14 days.
        // The trigger to start this schedule is the Scanning of the QR Code or the entry of the teleTAN.

        // schedule worker
        // inside worker, call playbook.dummy()

        TODO("")
    }

    private suspend fun runDummyPlaybook() {
        val runsToExecute =
            (SubmissionConstants.minNumberOfSequentialPlaybooks..SubmissionConstants.maxNumberOfSequentialPlaybooks).random()

        repeat(runsToExecute) {
            val secondsToWaitBetweenPlaybooks =
                (SubmissionConstants.minDelayBetweenSequentialPlaybooks..SubmissionConstants.maxDelayBetweenSequentialPlaybooks).random()
            playbook.dummy()
            delay(secondsToWaitBetweenPlaybooks * 1000L)
        }
    }

    suspend fun foregroundScheduleCheck() {
        if (LocalData.isAllowedToSubmitDiagnosisKeys() == true) {
            val chance = Random.nextFloat() * 100
            if (chance < SubmissionConstants.probabilityToExecutePlaybookWhenOpenApp) {
                runDummyPlaybook()
            }
        }
    }
}
