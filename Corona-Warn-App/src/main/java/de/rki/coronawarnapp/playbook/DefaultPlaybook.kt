package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationData
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.exception.TanPairingException
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.submission.server.SubmissionServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    internal var authCode: String? = null
    private val mutex = Mutex()

    override suspend fun initialRegistration(
        tokenRequest: RegistrationRequest
    ): RegistrationData {
        Timber.i("[$uid] New Initial Registration Playbook")

        // real registration
        val (registrationToken, registrationException) = executeCapturingExceptions {
            verificationServer.retrieveRegistrationToken(tokenRequest)
        }

        // if the registration succeeded continue with the real test result retrieval
        // if it failed, execute a dummy request to satisfy the required playbook pattern
        val (testResultResponse, testResultException) = if (registrationToken != null) {
            executeCapturingExceptions { verificationServer.pollTestResult(registrationToken) }
        } else {
            ignoreExceptions { verificationServer.retrieveTanFake() }
            null to null
        }

        // fake submission
        ignoreExceptions { submissionServer.submitFakePayload() }

        coroutineScope.launch { followUpPlaybooks() }

        // if registration and test result retrieval succeeded, return the result
        if (registrationToken != null && testResultResponse != null) {

            return RegistrationData(
                registrationToken = registrationToken,
                testResultResponse = testResultResponse,
            )
        }

        // else propagate the exception of either the first or the second step
        propagateException(registrationException, testResultException)
    }

    override suspend fun testResult(registrationToken: String): CoronaTestResultResponse {
        Timber.i("[$uid] New Test Result Playbook")

        // real test result
        val (testResult, exception) =
            executeCapturingExceptions { verificationServer.pollTestResult(registrationToken) }

        // fake verification
        ignoreExceptions { verificationServer.retrieveTanFake() }

        // fake submission
        ignoreExceptions { submissionServer.submitFakePayload() }

        coroutineScope.launch { followUpPlaybooks() }

        return testResult ?: propagateException(exception)
    }

    override suspend fun submit(
        data: Playbook.SubmissionData
    ) {
        mutex.withLock {
            Timber.i("[$uid] New Submission Playbook")
            var authCodeRequestException: Exception? = null
            if (authCode == null) {
                // real auth code
                val (newAuthCode, exception) = executeCapturingExceptions {
                    verificationServer.retrieveTan(data.registrationToken)
                }
                authCode = newAuthCode
                authCodeRequestException = exception
            } else {
                // fake request
                verificationServer.retrieveTanFake()
            }

            // fake verification
            ignoreExceptions { verificationServer.retrieveTanFake() }

            // submitKeysToServer could throw BadRequestException too.
            try {
                // real submission
                authCode?.let {
                    val serverSubmissionData = SubmissionServer.SubmissionData(
                        authCode = it,
                        keyList = data.temporaryExposureKeys,
                        consentToFederation = data.consentToFederation,
                        visitedCountries = data.visitedCountries,
                        unencryptedCheckIns = data.unencryptedCheckIns,
                        encryptedCheckIns = data.encryptedCheckIns,
                        submissionType = data.submissionType
                    )
                    submissionServer.submitPayload(serverSubmissionData)
                    authCode = null
                    coroutineScope.launch { followUpPlaybooks() }
                } ?: run {
                    submissionServer.submitFakePayload()
                    coroutineScope.launch { followUpPlaybooks() }
                    propagateException(wrapException(authCodeRequestException))
                }
            } catch (exception: BadRequestException) {
                propagateException(
                    TanPairingException(
                        code = exception.statusCode,
                        message = "Invalid payload or missing header",
                        cause = exception
                    )
                )
            }
        }
    }

    /**
     * Distinguish BadRequestException to present more insightful message to the end user
     */
    private fun wrapException(exception: Exception?) = when (exception) {
        is BadRequestException -> TanPairingException(
            code = exception.statusCode,
            message = "Tan has been retrieved before for this registration token",
            cause = exception
        )
        else -> exception
    }

    private suspend fun dummy(launchFollowUp: Boolean) {
        // fake verification
        ignoreExceptions { verificationServer.retrieveTanFake() }

        // fake verification
        ignoreExceptions { verificationServer.retrieveTanFake() }

        // fake submission
        ignoreExceptions { submissionServer.submitFakePayload() }

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
            Timber.v("Ignoring dummy request exception: %s", e.toString())
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
