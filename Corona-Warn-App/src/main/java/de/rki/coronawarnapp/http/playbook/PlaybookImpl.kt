package de.rki.coronawarnapp.http.playbook

import KeyExportFormat
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.util.formatter.TestResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class PlaybookImpl(
    private val webRequestBuilder: WebRequestBuilder
) : Playbook {

    private val uid = UUID.randomUUID().toString()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun initialRegistration(key: String, keyType: KeyType): String {
        Timber.i("[$uid] New Initial Registration Playbook")

        // real registration
        val (registrationToken, exception) =
            executeCapturingExceptions { webRequestBuilder.asyncGetRegistrationToken(key, keyType) }

        // fake verification
        ignoreExceptions { webRequestBuilder.asyncFakeVerification() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmission() }

        coroutineScope.launch { followUpPlaybooks() }

        return registrationToken ?: propagateException(exception)
    }

    override suspend fun testResult(registrationToken: String): TestResult {
        Timber.i("[$uid] New Test Result Playbook")

        // real test result
        val (testResult, exception) =
            executeCapturingExceptions { webRequestBuilder.asyncGetTestResult(registrationToken) }

        // fake verification
        ignoreExceptions { webRequestBuilder.asyncFakeVerification() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmission() }

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

        // fake verification
        ignoreExceptions { webRequestBuilder.asyncFakeVerification() }

        // real submission
        if (authCode != null) {
            webRequestBuilder.asyncSubmitKeysToServer(authCode, keys)
            coroutineScope.launch { followUpPlaybooks() }
        } else {
            webRequestBuilder.asyncFakeSubmission()
            coroutineScope.launch { followUpPlaybooks() }
            propagateException(exception)
        }
    }

    private suspend fun dummy(launchFollowUp: Boolean) {
        // fake verification
        ignoreExceptions { webRequestBuilder.asyncFakeVerification() }

        // fake verification
        ignoreExceptions { webRequestBuilder.asyncFakeVerification() }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmission() }

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
