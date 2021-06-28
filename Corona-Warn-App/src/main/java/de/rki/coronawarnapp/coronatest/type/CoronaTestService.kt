package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationData
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.deniability.NoiseScheduler
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber
import javax.inject.Inject

class CoronaTestService @Inject constructor(
    private val playbook: Playbook,
    private val noiseScheduler: NoiseScheduler,
) {

    suspend fun checkTestResult(registrationToken: String): CoronaTestResultResponse {
        return playbook.testResult(registrationToken)
    }

    suspend fun registerTest(tokenRequest: RegistrationRequest): RegistrationData {
        val response = playbook.initialRegistration(tokenRequest)

        Timber.d("Scheduling background noise.")
        scheduleDummyPattern()

        return response
    }

    private fun scheduleDummyPattern() {
        if (BackgroundConstants.NUMBER_OF_DAYS_TO_RUN_PLAYBOOK > 0) {
            noiseScheduler.setPeriodicNoise(enabled = true)
        }
    }
}
