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
        val (registrationToken, exception) =
            executeCapturingExceptions { webRequestBuilder.asyncGetRegistrationToken(key, keyType) }

        // fake test result
        ignoreExceptions { webRequestBuilder.asyncFakeGetTestResult() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmitKeysToServer() }

        return registrationToken ?: propagateException(exception)
    }

    override suspend fun testResult(registrationToken: String): TestResult {
        // real test result
        val (testResult, exception) =
            executeCapturingExceptions { webRequestBuilder.asyncGetTestResult(registrationToken) }

        // fake registration
        ignoreExceptions { webRequestBuilder.asyncFakeGetRegistrationToken() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmitKeysToServer() }

        return testResult?.let { TestResult.fromInt(it) }
            ?: propagateException(exception)
    }

    override suspend fun submission(
        registrationToken: String,
        keys: List<KeyExportFormat.TemporaryExposureKey>
    ) {
        // real auth code
        val (authCode, exception) = executeCapturingExceptions {
            webRequestBuilder.asyncGetTan(
                registrationToken
            )
        }

        // fake registration
        ignoreExceptions { webRequestBuilder.asyncFakeGetRegistrationToken() }

        // real submission
        if (authCode != null) {
            webRequestBuilder.asyncSubmitKeysToServer(authCode, keys)
        } else {
            webRequestBuilder.asyncFakeSubmitKeysToServer()
            propagateException(exception)
        }
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

    private suspend fun <T> executeCapturingExceptions(body: suspend () -> T): Pair<T?, Exception?> {
        return try {
            val result = body.invoke()
            result to null
        } catch (e: Exception) {
            null to e
        }
    }

    private fun propagateException(exception: Exception?): Nothing {
        throw exception ?: IllegalStateException()
    }
}
