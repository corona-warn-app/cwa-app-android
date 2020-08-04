package de.rki.coronawarnapp.http.playbook

import KeyExportFormat
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.util.formatter.TestResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class PlaybookImpl(
    private val webRequestBuilder: WebRequestBuilder,
    private val coroutineScope: CoroutineScope
) : Playbook {

    private val uid = UUID.randomUUID().toString()

    override suspend fun initialRegistration(key: String, keyType: KeyType): String {
        Timber.i("[$uid] New Initial Registration Playbook")

        // real registration
        val (registrationToken, exception) =
            executeCapturingExceptions { webRequestBuilder.asyncGetRegistrationToken(key, keyType) }

        // fake test result
        ignoreExceptions { webRequestBuilder.asyncFakeGetTestResult() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmitKeysToServer() }

        coroutineScope.launch { followUpPlaybooks() }

        return registrationToken ?: propagateException(exception)
    }

    override suspend fun testResult(registrationToken: String): TestResult {
        Timber.i("[$uid] New Test Result Playbook")

        // real test result
        val (testResult, exception) =
            executeCapturingExceptions { webRequestBuilder.asyncGetTestResult(registrationToken) }

        // fake registration
        ignoreExceptions { webRequestBuilder.asyncFakeGetRegistrationToken() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmitKeysToServer() }

        coroutineScope.launch { followUpPlaybooks() }

        return testResult?.let { TestResult.fromInt(it) }
            ?: propagateException(exception)
    }

    override suspend fun submission(
        registrationToken: String,
        keys: List<KeyExportFormat.TemporaryExposureKey>
    ) {
        Timber.i("[$uid] New Submission Playbook")

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
            coroutineScope.launch { followUpPlaybooks() }
        } else {
            webRequestBuilder.asyncFakeSubmitKeysToServer()
            coroutineScope.launch { followUpPlaybooks() }
            propagateException(exception)
        }
    }

    private suspend fun dummy(launchFollowUp: Boolean) {
        // fake registration
        ignoreExceptions { webRequestBuilder.asyncFakeGetRegistrationToken() }

        // fake test result
        ignoreExceptions { webRequestBuilder.asyncFakeGetTestResult() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmitKeysToServer() }

        if (launchFollowUp)
            coroutineScope.launch { followUpPlaybooks() }
    }

    override suspend fun dummy() = dummy(true)

    private suspend fun followUpPlaybooks() {
        val runsToExecute = Random.nextInt(
            SubmissionConstants.minNumberOfSequentialPlaybooks,
            SubmissionConstants.maxNumberOfSequentialPlaybooks
        )
        Timber.i("[$uid] Follow Up: launching $runsToExecute follow up playbooks")

        repeat(runsToExecute) {
            val executionDelay = Random.nextInt(
                SubmissionConstants.minDelayBetweenSequentialPlaybooks,
                SubmissionConstants.maxDelayBetweenSequentialPlaybooks
            )
            Timber.i("[$uid] Follow Up: (${it + 1}/$runsToExecute) waiting $executionDelay[s]...")
            delay(TimeUnit.SECONDS.toMillis(executionDelay.toLong()))

            dummy(false)
        }
        Timber.i("[$uid] Follow Up: finished")
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
