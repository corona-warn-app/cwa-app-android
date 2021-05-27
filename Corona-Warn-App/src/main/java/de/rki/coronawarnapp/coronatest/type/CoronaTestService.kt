package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.deniability.NoiseScheduler
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber
import javax.inject.Inject

class CoronaTestService @Inject constructor(
    private val playbook: Playbook,
    private val noiseScheduler: NoiseScheduler,
) {

    suspend fun asyncRequestTestResult(registrationToken: String): CoronaTestResultResponse {
        return playbook.testResult(registrationToken)
    }

    suspend fun asyncRegisterDeviceViaGUID(guid: String): RegistrationData {
        val (registrationToken, testResult) =
            playbook.initialRegistration(
                guid,
                VerificationKeyType.GUID
            )

        Timber.d("Scheduling background noise.")
        scheduleDummyPattern()

        return RegistrationData(registrationToken, testResult)
    }

    suspend fun asyncRegisterDeviceViaTAN(tan: String): RegistrationData {
        val (registrationToken, testResult) =
            playbook.initialRegistration(
                tan,
                VerificationKeyType.TELETAN
            )

        Timber.d("Scheduling background noise.")
        scheduleDummyPattern()

        return RegistrationData(registrationToken, testResult)
    }

    private fun scheduleDummyPattern() {
        if (BackgroundConstants.NUMBER_OF_DAYS_TO_RUN_PLAYBOOK > 0) {
            noiseScheduler.setPeriodicNoise(enabled = true)
        }
    }

    data class RegistrationData(
        val registrationToken: String,
        val testResultResponse: CoronaTestResultResponse
    )
}
