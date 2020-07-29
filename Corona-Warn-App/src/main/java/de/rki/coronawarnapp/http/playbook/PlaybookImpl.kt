package de.rki.coronawarnapp.http.playbook

import KeyExportFormat
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.util.formatter.TestResult

class PlaybookImpl : Playbook {
    override suspend fun initialRegistration(key: String, keyType: KeyType): String {
        // real registration
        val registrationToken =
            WebRequestBuilder.getInstance().asyncGetRegistrationToken(key, keyType)
        // fake test result
        WebRequestBuilder.getInstance().asyncFakeGetTestResult(registrationToken, "?????????")
        // fake auth code
        // TODO: Proper request padding
        val fakeAuthCode =
            WebRequestBuilder.getInstance().asyncFakeGetTan(registrationToken, "?????????")
        // fake submission
        // TODO: Generate fake keys
        WebRequestBuilder.getInstance().asyncFakeSubmitKeysToServer(fakeAuthCode, listOf())
        return registrationToken
    }

    override suspend fun testResult(registrationToken: String): TestResult {
        // fake registration
        // TODO: Decision about fake key type and padding
        val fakeRegistrationToken = WebRequestBuilder.getInstance()
            .asyncFakeGetRegistrationToken("0123456789", KeyType.TELETAN, "?????????")
        // real test result
        val testResult = WebRequestBuilder.getInstance().asyncGetTestResult(registrationToken)
        // fake auth code
        // TODO: Proper request padding
        val fakeAuthCode =
            WebRequestBuilder.getInstance().asyncFakeGetTan(fakeRegistrationToken, "?????????")
        // fake submission
        // TODO: Generate fake keys
        WebRequestBuilder.getInstance().asyncFakeSubmitKeysToServer(fakeAuthCode, listOf())
        return TestResult.fromInt(testResult)
    }

    override suspend fun submission(
        registrationToken: String,
        keys: List<KeyExportFormat.TemporaryExposureKey>
    ) {
        // fake registration
        // TODO: Decision about fake key type and padding
        val fakeRegistrationToken = WebRequestBuilder.getInstance()
            .asyncFakeGetRegistrationToken("0123456789", KeyType.TELETAN, "?????????")
        // fake test result
        // TODO: Proper request padding
        WebRequestBuilder.getInstance().asyncFakeGetTestResult(fakeRegistrationToken, "?????????")
        // real auth code
        val authCode = WebRequestBuilder.getInstance().asyncGetTan(registrationToken)
        // real submission
        WebRequestBuilder.getInstance().asyncFakeSubmitKeysToServer(authCode, keys)
    }

    override suspend fun dummy() {
        // fake registration
        // TODO: Decision about fake key type and padding
        val fakeRegistrationToken = WebRequestBuilder.getInstance()
            .asyncFakeGetRegistrationToken("0123456789", KeyType.TELETAN, "?????????")
        // fake test result
        WebRequestBuilder.getInstance().asyncFakeGetTestResult(fakeRegistrationToken, "?????????")
        // fake auth code
        // TODO: Proper request padding
        val fakeAuthCode =
            WebRequestBuilder.getInstance().asyncFakeGetTan(fakeRegistrationToken, "?????????")
        // fake submission
        // TODO: Generate fake keys
        WebRequestBuilder.getInstance().asyncFakeSubmitKeysToServer(fakeAuthCode, listOf())
    }
}
