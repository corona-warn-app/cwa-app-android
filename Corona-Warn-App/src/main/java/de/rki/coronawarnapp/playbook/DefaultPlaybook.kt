package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.submission.server.SubmissionServer
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.verification.server.VerificationKeyType
import de.rki.coronawarnapp.verification.server.VerificationServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPlaybook @Inject constructor(
    private val verificationServer: VerificationServer,
    private val submissionServer: SubmissionServer
) : Playbook {

    private val uid = UUID.randomUUID().toString()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun initialRegistration(
        key: String,
        keyType: VerificationKeyType
    ): Pair<String, TestResult> {
        Timber.i("[$uid] New Initial Registration Playbook")

        // real registration
        val (registrationToken, registrationException) =
            executeCapturingExceptions {
                verificationServer.asyncGetRegistrationToken(
                    key,
                    keyType
                )
            }

        // if the registration succeeded continue with the real test result retrieval
        // if it failed, execute a dummy request to satisfy the required playbook pattern
        val (testResult, testResultException) = if (registrationToken != null) {
            executeCapturingExceptions { verificationServer.asyncGetTestResult(registrationToken) }
        } else {
            ignoreExceptions { verificationServer.asyncFakeVerification() }
            null to null
        }

        // fake submission
        ignoreExceptions { submissionServer.submitKeysToServerFake() }

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
            executeCapturingExceptions { verificationServer.asyncGetTestResult(registrationToken) }

        // fake verification
        ignoreExceptions { verificationServer.asyncFakeVerification() }

        // fake submission
        ignoreExceptions { submissionServer.submitKeysToServerFake() }

        coroutineScope.launch { followUpPlaybooks() }

        return testResult?.let { TestResult.fromInt(it) }
            ?: propagateException(exception)
    }

    override suspend fun submission(
        data: Playbook.SubmissionData
    ) {
        Timber.i("[$uid] New Submission Playbook")
        // real auth code
        val (authCode, exception) = executeCapturingExceptions {
            verificationServer.asyncGetTan(data.registrationToken)
        }

        // fake verification
        ignoreExceptions { verificationServer.asyncFakeVerification() }

        // real submission
        if (authCode != null) {
            val serverSubmissionData = SubmissionServer.SubmissionData(
                authCode = authCode,
                keyList = data.temporaryExposureKeys,
                consentToFederation = data.consentToFederation,
                visistedCountries = data.visistedCountries
            )
            submissionServer.submitKeysToServer(serverSubmissionData)
            coroutineScope.launch { followUpPlaybooks() }
        } else {
            submissionServer.submitKeysToServerFake()
            coroutineScope.launch { followUpPlaybooks() }
            propagateException(exception)
        }
    }

    private suspend fun dummy(launchFollowUp: Boolean) {
        // fake verification
        ignoreExceptions { verificationServer.asyncFakeVerification() }

        // fake verification
        ignoreExceptions { verificationServer.asyncFakeVerification() }

        // fake submission
        ignoreExceptions { submissionServer.submitKeysToServerFake() }

        if (launchFollowUp)
            coroutineScope.launch { followUpPlaybooks() }
    }

    override suspend fun dummy() = dummy(true)

    private suspend fun followUpPlaybooks() {
        val runsToExecute = IntRange(
            MIN_NUMBER_OF_SEQUENTIAL_PLAYBOOKS - 1 /* one was already executed */,
            MAX_NUMBER_OF_SEQUENTIAL_PLAYBOOKS - 1 /* one was already executed */
        ).random()
        Timber.i("[$uid] Follow Up: launching $runsToExecute follow up playbooks")

        repeat(runsToExecute) {
            val executionDelay = IntRange(
                MIN_DELAY_BETWEEN_SEQUENTIAL_PLAYBOOKS,
                MAX_DELAY_BETWEEN_SEQUENTIAL_PLAYBOOKS
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

    companion object {
        const val PROBABILITY_TO_EXECUTE_PLAYBOOK_ON_APP_OPEN = 0f
        const val MIN_NUMBER_OF_SEQUENTIAL_PLAYBOOKS = 1
        const val MAX_NUMBER_OF_SEQUENTIAL_PLAYBOOKS = 1
        const val MIN_DELAY_BETWEEN_SEQUENTIAL_PLAYBOOKS = 0
        const val MAX_DELAY_BETWEEN_SEQUENTIAL_PLAYBOOKS = 0
    }
}
