package de.rki.coronawarnapp.http.playbook

import KeyExportFormat
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.util.formatter.TestResult

class PlaybookImpl(
    private val webRequestBuilder: WebRequestBuilder
) : Playbook {

    override suspend fun initialRegistration(key: String, keyType: KeyType): String {
        // real registration
        val registrationToken = webRequestBuilder.asyncGetRegistrationToken(key, keyType)

        // fake test result
        webRequestBuilder.asyncFakeGetTestResult()

        // fake submission
        webRequestBuilder.asyncFakeSubmitKeysToServer()

        return registrationToken
    }

    override suspend fun testResult(registrationToken: String): TestResult {
        // real test result
        val testResult = webRequestBuilder.asyncGetTestResult(registrationToken)

        // fake registration
        webRequestBuilder.asyncFakeGetRegistrationToken()

        // fake submission
        webRequestBuilder.asyncFakeSubmitKeysToServer()

        return TestResult.fromInt(testResult)
    }

    override suspend fun submission(
        registrationToken: String,
        keys: List<KeyExportFormat.TemporaryExposureKey>
    ) {
        // real auth code
        val authCode = webRequestBuilder.asyncGetTan(registrationToken)

        // fake registration
        webRequestBuilder.asyncFakeGetRegistrationToken()

        // real submission
        webRequestBuilder.asyncSubmitKeysToServer(authCode, keys)
    }

    override suspend fun dummy() {
        // fake registration
        webRequestBuilder.asyncFakeGetRegistrationToken()

        // fake test result
        webRequestBuilder.asyncFakeGetTestResult()

        // fake submission
        webRequestBuilder.asyncFakeSubmitKeysToServer()
    }
}
