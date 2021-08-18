package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.exception.TanPairingException
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.OrganizerSubmissionServer
import de.rki.coronawarnapp.submission.server.SubmissionServer
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OrganizerPlaybook @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val verificationServer: VerificationServer,
    private val organizerSubmissionServer: OrganizerSubmissionServer,
    private val submissionServer: SubmissionServer,
    dispatcherProvider: DispatcherProvider,
) {
    private val uid = UUID.randomUUID().toString()
    private val coroutineScope: CoroutineScope = appScope + dispatcherProvider.IO

    suspend fun submit(tan: String, checkInsReport: CheckInsReport) {
        Timber.i("[$uid] New Submission Playbook")
        // Real upload TAN
        val (uploadTan, exception) = executeCapturingExceptions {
            obtainUploadTan(RegistrationRequest(key = tan, type = VerificationKeyType.TELETAN))
        }

        // Fake verification
        ignoreExceptions { verificationServer.retrieveTanFake() }
        try {
            // Real submission
            if (uploadTan != null) {
                organizerSubmissionServer.submit(uploadTan, checkInsReport)
                coroutineScope.launch { followUpPlaybooks() }
            } else {
                submissionServer.submitFakePayload()
                coroutineScope.launch { followUpPlaybooks() }
                propagateException(wrapException(exception))
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

    private suspend fun obtainUploadTan(
        tokenRequest: RegistrationRequest
    ): String {
        Timber.i("[$uid] New Initial Registration Playbook")

        // Real registration token
        val (registrationToken, registrationException) = executeCapturingExceptions {
            verificationServer.retrieveRegistrationToken(tokenRequest)
        }

        // if the registration succeeded continue with the real upload TAN retrieval
        // if it failed, execute a dummy request to satisfy the required playbook pattern
        val (uploadTan, uploadTanException) = if (registrationToken != null) {
            executeCapturingExceptions { verificationServer.retrieveTan(registrationToken) }
        } else {
            ignoreExceptions { verificationServer.retrieveTanFake() }
            null to null
        }

        // fake submission
        ignoreExceptions { submissionServer.submitFakePayload() }

        coroutineScope.launch { followUpPlaybooks() }

        // if registration and test result retrieval succeeded, return the result
        if (registrationToken != null && uploadTan != null) {
            return uploadTan
        }

        // else propagate the exception of either the first or the second step
        propagateException(registrationException, uploadTanException)
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

    private suspend fun dummy() {
        // fake verification
        ignoreExceptions { verificationServer.retrieveTanFake() }

        // fake verification
        ignoreExceptions { verificationServer.retrieveTanFake() }

        // fake submission
        ignoreExceptions { submissionServer.submitFakePayload() }
    }

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

            dummy()
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
        private const val MIN_NUMBER_OF_SEQUENTIAL_PLAYBOOKS = 1
        private const val MAX_NUMBER_OF_SEQUENTIAL_PLAYBOOKS = 1
        private const val MIN_DELAY_BETWEEN_SEQUENTIAL_PLAYBOOKS = 0
        private const val MAX_DELAY_BETWEEN_SEQUENTIAL_PLAYBOOKS = 0
    }
}
