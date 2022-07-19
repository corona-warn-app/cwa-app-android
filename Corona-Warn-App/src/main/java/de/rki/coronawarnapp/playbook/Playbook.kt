package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationData
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.exception.TanPairingException
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.submission.server.SubmissionServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The concept of Plausible Deniability aims to hide the existence of a positive test result by always using a defined
 * “playbook pattern” of requests to the Verification Server and CWA Backend so it is impossible for an attacker to
 * identify which communication was done.
 *
 * The “playbook pattern” represents a well-defined communication pattern consisting of fake requests and real
 * requests.
 *
 * To hide that a real request was done, the device does multiple of these requests over a longer period of time
 * according to the previously defined communication pattern statistically similar to all apps so it is not possible to
 * infer by observing the traffic if the requests under concern are real or the fake ones.
 */

@Singleton
class Playbook @Inject constructor(
    private val verificationServer: VerificationServer,
    private val submissionServer: SubmissionServer
) {

    private val uid = UUID.randomUUID().toString()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun initialRegistration(
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

    suspend fun testResult(registrationToken: String): CoronaTestResultResponse {
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

    suspend fun retrieveTan(registrationToken: String, authCode: String?): String {
        return if (authCode == null) {
            // real auth code
            val (newAuthCode, exception) = executeCapturingExceptions {
                verificationServer.retrieveTan(registrationToken)
            }
            if (newAuthCode == null) {
                propagateException(wrapException(exception))
            }
            newAuthCode
        } else {
            // fake request
            verificationServer.retrieveTanFake()
            authCode
        }
    }

    suspend fun submit(
        data: SubmissionData?
    ) {
        Timber.i("[$uid] New Submission Playbook")

        // fake verification
        ignoreExceptions { verificationServer.retrieveTanFake() }

        // submitKeysToServer could throw BadRequestException too.
        try {
            // real submission
            data?.authCode?.let {
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
                coroutineScope.launch { followUpPlaybooks() }
            } ?: run {
                submissionServer.submitFakePayload()
                coroutineScope.launch { followUpPlaybooks() }
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

    suspend fun dummy() = dummy(true)

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
        const val MIN_NUMBER_OF_SEQUENTIAL_PLAYBOOKS = 1
        const val MAX_NUMBER_OF_SEQUENTIAL_PLAYBOOKS = 1
        const val MIN_DELAY_BETWEEN_SEQUENTIAL_PLAYBOOKS = 0
        const val MAX_DELAY_BETWEEN_SEQUENTIAL_PLAYBOOKS = 0
    }

    data class SubmissionData(
        val registrationToken: String,
        val authCode: String? = null,
        val temporaryExposureKeys: List<TemporaryExposureKeyExportOuterClass.TemporaryExposureKey>,
        val consentToFederation: Boolean,
        val visitedCountries: List<String>,
        val unencryptedCheckIns: List<CheckInOuterClass.CheckIn>,
        val encryptedCheckIns: List<CheckInOuterClass.CheckInProtectedReport>,
        val submissionType: SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType
    )
}
