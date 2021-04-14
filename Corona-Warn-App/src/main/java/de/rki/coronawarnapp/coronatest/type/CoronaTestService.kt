package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.playbook.Playbook
import javax.inject.Inject

class CoronaTestService @Inject constructor(
    private val playbook: Playbook
) {

    suspend fun asyncRequestTestResult(registrationToken: String): CoronaTestResult {
        return playbook.testResult(registrationToken)
    }

    suspend fun asyncRegisterDeviceViaGUID(guid: String): RegistrationData {
        val (registrationToken, testResult) =
            playbook.initialRegistration(
                guid,
                VerificationKeyType.GUID
            )
        return RegistrationData(registrationToken, testResult)
    }

    suspend fun asyncRegisterDeviceViaTAN(tan: String): RegistrationData {
        val (registrationToken, testResult) =
            playbook.initialRegistration(
                tan,
                VerificationKeyType.TELETAN
            )
        return RegistrationData(registrationToken, testResult)
    }

    data class RegistrationData(
        val registrationToken: String,
        val testResult: CoronaTestResult
    )
}
