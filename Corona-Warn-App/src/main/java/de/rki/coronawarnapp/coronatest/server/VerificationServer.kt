package de.rki.coronawarnapp.coronatest.server

import dagger.Lazy
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.util.PaddingTool.requestPadding
import de.rki.coronawarnapp.util.security.HashHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationServer @Inject constructor(
    private val verificationAPI: Lazy<VerificationApiV1>
) {

    private val api: VerificationApiV1
        get() = verificationAPI.get()

    suspend fun retrieveRegistrationToken(
        key: String,
        keyType: VerificationKeyType
    ): RegistrationToken = withContext(Dispatchers.IO) {
        val keyStr = if (keyType == VerificationKeyType.GUID) {
            HashHelper.hash256(key)
        } else {
            key
        }

        val paddingLength = when (keyType) {
            VerificationKeyType.GUID -> PADDING_LENGTH_BODY_REGISTRATION_TOKEN_GUID
            VerificationKeyType.TELETAN -> PADDING_LENGTH_BODY_REGISTRATION_TOKEN_TELETAN
        }

        api.getRegistrationToken(
            fake = "0",
            headerPadding = requestPadding(PADDING_LENGTH_HEADER_REGISTRATION_TOKEN),
            requestBody = VerificationApiV1.RegistrationTokenRequest(
                keyType = keyType.name,
                key = keyStr,
                requestPadding = requestPadding(paddingLength)
            )
        ).registrationToken
    }

    suspend fun pollTestResult(
        token: RegistrationToken
    ): CoronaTestResult = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("retrieveTestResults(token=%s)", token)
        val response = api.getTestResult(
            fake = "0",
            headerPadding = requestPadding(PADDING_LENGTH_HEADER_TEST_RESULT),
            request = VerificationApiV1.RegistrationRequest(
                token,
                requestPadding(PADDING_LENGTH_BODY_TEST_RESULT)
            )
        )

        Timber.tag(TAG).d("retrieveTestResults(token=%s) -> %s", token, response)
        CoronaTestResult.fromInt(response.testResult)
    }

    suspend fun retrieveTan(
        registrationToken: RegistrationToken
    ): String = withContext(Dispatchers.IO) {
        api.getTAN(
            fake = "0",
            headerPadding = requestPadding(PADDING_LENGTH_HEADER_TAN),
            requestBody = VerificationApiV1.TanRequestBody(
                registrationToken,
                requestPadding(PADDING_LENGTH_BODY_TAN)
            )
        ).tan
    }

    suspend fun retrieveTanFake() = withContext(Dispatchers.IO) {
        api.getTAN(
            fake = "1",
            headerPadding = requestPadding(PADDING_LENGTH_HEADER_TAN),
            requestBody = VerificationApiV1.TanRequestBody(
                registrationToken = DUMMY_REGISTRATION_TOKEN,
                requestPadding = requestPadding(PADDING_LENGTH_BODY_TAN_FAKE)
            )
        )
    }

    companion object {
        // padding registration token
        private const val VERIFICATION_BODY_FILL = 139

        const val PADDING_LENGTH_HEADER_REGISTRATION_TOKEN = 0
        const val PADDING_LENGTH_BODY_REGISTRATION_TOKEN_TELETAN = 51 + VERIFICATION_BODY_FILL
        const val PADDING_LENGTH_BODY_REGISTRATION_TOKEN_GUID = 0 + VERIFICATION_BODY_FILL

        // padding test result
        const val PADDING_LENGTH_HEADER_TEST_RESULT = 7
        const val PADDING_LENGTH_BODY_TEST_RESULT = 31 + VERIFICATION_BODY_FILL

        // padding tan
        const val PADDING_LENGTH_HEADER_TAN = 14
        const val PADDING_LENGTH_BODY_TAN = 31 + VERIFICATION_BODY_FILL
        const val PADDING_LENGTH_BODY_TAN_FAKE = 31 + VERIFICATION_BODY_FILL
        const val DUMMY_REGISTRATION_TOKEN = "11111111-2222-4444-8888-161616161616"

        private const val TAG = "VerificationServer"
    }
}
