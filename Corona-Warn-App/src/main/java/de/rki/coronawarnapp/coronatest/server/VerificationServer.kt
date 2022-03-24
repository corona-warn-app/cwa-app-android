package de.rki.coronawarnapp.coronatest.server

import dagger.Lazy
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.util.PaddingTool
import de.rki.coronawarnapp.util.security.HashHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationServer @Inject constructor(
    private val verificationAPI: Lazy<VerificationApiV1>,
    private val paddingTool: PaddingTool,
) {

    private val api: VerificationApiV1
        get() = verificationAPI.get()

    suspend fun retrieveRegistrationToken(
        request: RegistrationRequest
    ): RegistrationToken = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v("retrieveRegistrationToken(request=%s)", request)

        val requiredHeaderPadding = run {
            var size = HEADER_SIZE_OUR_DATA
            size -= HEADER_SIZE_OVERHEAD

            // `POST /version/v1/registrationToken`
            size -= 34

            size
        }
        val requiredBodyPadding = run {
            var size = BODY_SIZE_EXPECTED
            size -= BODY_SIZE_OVERHEAD

            size -= when (request.type) {
                VerificationKeyType.GUID -> 17 // `"keyType":"GUID",`
                VerificationKeyType.TELETAN -> 20 // `"keyType":"TELETAN",`
            }

            size -= when (request.type) {
                VerificationKeyType.GUID -> {
                    73 // `"key":"75552e6e1dae7a520bad64e92b7569447d0f5ca3c539335e0418a7695606147e",`
                }
                VerificationKeyType.TELETAN -> {
                    19 // `"key":"ERYCJMM4DC",`
                }
            }

            size -= when (request.dateOfBirthKey) {
                null -> 0
                else -> 76 // `"keyDob":"x9acafb78b330522e32b4bf4c90a3ebb7a4d20d8af8cc32018c550ea86a38cc1",`
            }
            size
        }

        val response = api.getRegistrationToken(
            fake = "0",
            headerPadding = paddingTool.requestPadding(requiredHeaderPadding),
            requestBody = VerificationApiV1.RegistrationTokenRequest(
                keyType = request.type,
                key = when (request.type) {
                    VerificationKeyType.GUID -> HashHelper.hash256(request.key)
                    else -> request.key
                },
                dateOfBirthKey = request.dateOfBirthKey?.key,
                requestPadding = paddingTool.requestPadding(requiredBodyPadding),
            )
        )

        Timber.tag(TAG).d("retrieveRegistrationToken(request=%s) -> %s", request, response)
        response.registrationToken
    }

    suspend fun pollTestResult(
        token: RegistrationToken
    ): CoronaTestResultResponse = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v("pollTestResult(token=%s)", token)

        val requiredHeaderPadding = run {
            var size = HEADER_SIZE_OUR_DATA
            size -= HEADER_SIZE_OVERHEAD

            // `POST /version/v1/testresult`
            size -= 27

            size
        }
        val requiredBodyPadding = run {
            var size = BODY_SIZE_EXPECTED
            size -= BODY_SIZE_OVERHEAD

            // `"registrationToken":"63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f",`
            size -= 59

            size
        }

        val response = api.getTestResult(
            fake = "0",
            headerPadding = paddingTool.requestPadding(requiredHeaderPadding),
            request = VerificationApiV1.RegistrationRequest(
                token,
                paddingTool.requestPadding(requiredBodyPadding)
            )
        )

        Timber.tag(TAG).d("pollTestResult(token=%s) -> %s", token, response)

        CoronaTestResultResponse.fromResponse(response)
    }

    suspend fun retrieveTan(
        registrationToken: RegistrationToken
    ): String = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v("retrieveTan(registrationToken=%s)", registrationToken)
        val requiredHeaderPadding = run {
            var size = HEADER_SIZE_OUR_DATA
            size -= HEADER_SIZE_OVERHEAD

            // `POST /version/v1/tan`
            size -= 20

            size
        }
        val requiredBodyPadding = run {
            var size = BODY_SIZE_EXPECTED
            size -= BODY_SIZE_OVERHEAD

            // `"registrationToken":"63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f",`
            size -= 59

            size
        }

        val response = api.getTAN(
            fake = "0",
            headerPadding = paddingTool.requestPadding(requiredHeaderPadding),
            requestBody = VerificationApiV1.TanRequestBody(
                registrationToken,
                paddingTool.requestPadding(requiredBodyPadding)
            )
        )

        Timber.tag(TAG).d("retrieveTan(registrationToken=%s) -> %s", registrationToken, response)
        response.tan
    }

    suspend fun retrieveTanFake() = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v("retrieveTanFake()")
        val requiredHeaderPadding = run {
            var size = HEADER_SIZE_OUR_DATA
            size -= HEADER_SIZE_OVERHEAD

            // `POST /version/v1/tan`
            size -= 20

            size
        }
        val requiredBodyPadding = run {
            var size = BODY_SIZE_EXPECTED
            size -= BODY_SIZE_OVERHEAD

            // `"registrationToken":"63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f",`
            size -= 59

            size
        }

        val response = api.getTAN(
            fake = "1",
            headerPadding = paddingTool.requestPadding(requiredHeaderPadding),
            requestBody = VerificationApiV1.TanRequestBody(
                registrationToken = DUMMY_REGISTRATION_TOKEN,
                requestPadding = paddingTool.requestPadding(requiredBodyPadding)
            )
        )
        Timber.tag(TAG).v("retrieveTanFake() -> %s", response)
        response
    }

    companion object {
        /**
         * The specific sizes are not important, but all requests should be padded up to the same size.
         * Pick a total size that is guaranteed to be above or equal to the maximum size a request can be.
         */
        // `"requestPadding":""`
        private const val BODY_SIZE_PADDING_OVERHEAD = 19 //

        // `{}` json brackets
        private const val BODY_SIZE_OVERHEAD = BODY_SIZE_PADDING_OVERHEAD + 2
        private const val BODY_SIZE_EXPECTED = 250

        /**
         * The header itself is larger.
         * We care about the header fields we set that are request specific.
         * We don't need to pad for device specific fields set by OK http.
         */
        // `POST /version/v1/registrationToken` -> 34 (longest method + url atm) use 64 to have a buffer
        private const val HEADER_SIZE_LONGEST_METHOD = 34

        // `cwa-fake 0\n` -> 12
        private const val HEADER_SIZE_VAL_FAKE = 12

        // `cwa-header-padding\n` -> 22
        private const val HEADER_SIZE_VAL_PADDING = 22
        private const val HEADER_SIZE_OVERHEAD = HEADER_SIZE_VAL_FAKE + HEADER_SIZE_VAL_PADDING
        private const val HEADER_SIZE_OUR_DATA = HEADER_SIZE_LONGEST_METHOD + HEADER_SIZE_OVERHEAD

        const val DUMMY_REGISTRATION_TOKEN = "11111111-2222-4444-8888-161616161616"

        /**
         * Test is available for this long on the server.
         * After this period the server will delete it and return PENDING if the regtoken is polled again.
         */
        val TestAvailabilityDuration: Duration = Duration.standardDays(60)

        private const val TAG = "VerificationServer"
    }
}
