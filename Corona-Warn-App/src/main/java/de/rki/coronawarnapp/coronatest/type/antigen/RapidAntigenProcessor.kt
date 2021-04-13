package de.rki.coronawarnapp.coronatest.type.antigen

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.util.TimeStamper
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RapidAntigenProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val verificationServer: VerificationServer,
) : CoronaTestProcessor<CoronaTestQRCode.RapidAntigen, RapidAntigenCoronaTest> {
    override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

    override suspend fun create(data: CoronaTestQRCode.RapidAntigen): RapidAntigenCoronaTest {
        Timber.tag(TAG).d("create(data=%s)", data)

        val registrationToken: RegistrationToken = verificationServer.retrieveRegistrationToken(
            key = data.guid,
            keyType = VerificationKeyType.GUID
        )

        return RapidAntigenCoronaTest(
            testGUID = data.guid,
            registeredAt = timeStamper.nowUTC,
            registrationToken = registrationToken,
            testResult = CoronaTestResult.RAT_PENDING,
            testedAt = data.createdAt,
            firstName = data.firstName,
            lastName = data.lastName,
            dateOfBirth = data.dateOfBirth,
        )
    }

    override suspend fun markSubmitted(test: RapidAntigenCoronaTest): RapidAntigenCoronaTest {
        Timber.tag(TAG).d("markSubmitted(test=%s)", test)
        return test.copy(
            isSubmitted = true
        )
    }

    companion object {
        private const val TAG = "RapidAntigenProcessor"
    }
}
