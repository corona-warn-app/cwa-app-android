package de.rki.coronawarnapp.verification.server

import dagger.Lazy
import de.rki.coronawarnapp.util.PaddingTool.requestPadding
import de.rki.coronawarnapp.util.security.HashHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    ): String = withContext(Dispatchers.IO) {
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

    suspend fun retrieveTestResults(
        registrationToken: String
    ): Int = withContext(Dispatchers.IO) {
        api.getTestResult(
            fake = "0",
            headerPadding = requestPadding(PADDING_LENGTH_HEADER_TEST_RESULT),
            request = VerificationApiV1.RegistrationRequest(
                registrationToken,
                requestPadding(PADDING_LENGTH_BODY_TEST_RESULT)
            )
        ).testResult
    }

    suspend fun retrieveTan(
        registrationToken: String
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
    }
}
