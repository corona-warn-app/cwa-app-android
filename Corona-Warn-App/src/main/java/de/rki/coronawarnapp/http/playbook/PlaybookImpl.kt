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

class PlaybookImpl(
    private val webRequestBuilder: WebRequestBuilder
) : Playbook {

    private val uid = UUID.randomUUID().toString()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun initialRegistration(
        key: String,
        keyType: KeyType
    ): Pair<String, TestResult> {
        Timber.i("[$uid] New Initial Registration Playbook")

        // real registration
        val (registrationToken, registrationException) =
            executeCapturingExceptions { webRequestBuilder.asyncGetRegistrationToken(key, keyType) }

        // if the registration succeeded continue with the real test result retrieval
        // if it failed, execute a dummy request to satisfy the required playbook pattern
        val (testResult, testResultException) = if (registrationToken != null) {
            executeCapturingExceptions { webRequestBuilder.asyncGetTestResult(registrationToken) }
        } else {
            ignoreExceptions { webRequestBuilder.asyncFakeVerification() }
            null to null
        }

        // fake submission
        ignoreExceptions { webRequestBuilder.asyncFakeSubmission() }

        coroutineScope.launch { followUpPlaybooks() }

        // if registration and test result retrieval succeeded, return the result
        if (registrationToken != null && testResult != null)
            return registrationToken to TestResult.fromInt(testResult)

        // else propagate the exception of either the first or the second step
        propagateException(registrationException, testResultException)
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
        visitedCountries: List<String>,
        consentToFederation: Boolean,
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
            webRequestBuilder.asyncSubmitKeysToServer(
                authCode,
                visitedCountries,
                consentToFederation,
                keys
            )
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
        val runsToExecute = IntRange(
            SubmissionConstants.minNumberOfSequentialPlaybooks - 1 /* one was already executed */,
            SubmissionConstants.maxNumberOfSequentialPlaybooks - 1 /* one was already executed */
        ).random()
        Timber.i("[$uid] Follow Up: launching $runsToExecute follow up playbooks")

        repeat(runsToExecute) {
            val executionDelay = IntRange(
                SubmissionConstants.minDelayBetweenSequentialPlaybooks,
                SubmissionConstants.maxDelayBetweenSequentialPlaybooks
            ).random()
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

    private fun propagateException(vararg exceptions: Exception?): Nothing {
        throw exceptions.filterNotNull().firstOrNull() ?: IllegalStateException()
    }
}
