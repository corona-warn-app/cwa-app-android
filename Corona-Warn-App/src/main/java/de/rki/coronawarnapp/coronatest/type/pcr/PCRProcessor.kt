package de.rki.coronawarnapp.coronatest.type.pcr

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
class PCRProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val verificationServer: VerificationServer,
) : CoronaTestProcessor<CoronaTestQRCode.PCR, PCRCoronaTest> {

    override val type: CoronaTest.Type = CoronaTest.Type.PCR

    override suspend fun create(data: CoronaTestQRCode.PCR): PCRCoronaTest {
        Timber.tag(TAG).d("create(data=%s)", data)

        val registrationToken: RegistrationToken = verificationServer.retrieveRegistrationToken(
            key = data.guid,
            keyType = VerificationKeyType.GUID
        )

        return PCRCoronaTest(
            testGUID = data.guid,
            registeredAt = timeStamper.nowUTC,
            registrationToken = registrationToken,
            testResult = CoronaTestResult.PCR_OR_RAT_PENDING,
        )
    }

    override suspend fun markSubmitted(test: PCRCoronaTest): PCRCoronaTest {
        Timber.tag(TAG).d("markSubmitted(test=%s)", test)
        return test.copy(
            isSubmitted = true
        )
    }

    companion object {
        private const val TAG = "PCRProcessor"
    }
}
