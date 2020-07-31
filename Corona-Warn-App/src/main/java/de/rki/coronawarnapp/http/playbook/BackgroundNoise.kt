package de.rki.coronawarnapp.http.playbook

import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import kotlinx.coroutines.runBlocking
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

    fun foregroundScheduleCheck() {
        val chance = Random.nextFloat() * 100
        if (chance < SubmissionConstants.probabilityToExecutePlaybookWhenOpenApp) {
            runBlocking {
                playbook.dummy()
            }
        }
    }
}
