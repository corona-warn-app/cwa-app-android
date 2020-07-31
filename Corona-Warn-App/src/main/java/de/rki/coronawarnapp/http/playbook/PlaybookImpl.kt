package de.rki.coronawarnapp.http.playbook

import KeyExportFormat
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.util.formatter.TestResult
import timber.log.Timber

class PlaybookImpl(
    private val webRequestBuilder: WebRequestBuilder
) : Playbook {

    override suspend fun initialRegistration(key: String, keyType: KeyType): String {
        // real registration
        val registrationToken = webRequestBuilder.asyncGetRegistrationToken(key, keyType)

        // fake test result
        ignoreExceptions { webRequestBuilder.asyncFakeGetTestResult() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmitKeysToServer() }

        return registrationToken
    }

    override suspend fun testResult(registrationToken: String): TestResult {
        // real test result
        val testResult = webRequestBuilder.asyncGetTestResult(registrationToken)

        // fake registration
        ignoreExceptions { webRequestBuilder.asyncFakeGetRegistrationToken() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmitKeysToServer() }

        return TestResult.fromInt(testResult)
    }

    override suspend fun submission(
        registrationToken: String,
        keys: List<KeyExportFormat.TemporaryExposureKey>
    ) {
        // real auth code
        val authCode = webRequestBuilder.asyncGetTan(registrationToken)

        // fake registration
        ignoreExceptions { webRequestBuilder.asyncFakeGetRegistrationToken() }

        // real submission
        webRequestBuilder.asyncSubmitKeysToServer(authCode, keys)
    }

    override suspend fun dummy() {
        // fake registration
        ignoreExceptions { webRequestBuilder.asyncFakeGetRegistrationToken() }

        // fake test result
        ignoreExceptions { webRequestBuilder.asyncFakeGetTestResult() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmitKeysToServer() }
    }

    private suspend fun ignoreExceptions(body: suspend () -> Unit) {
        try {
            body.invoke()
        } catch (e: Exception) {
            Timber.d(e, "Ignoring dummy request exception")
        }
    }
}
