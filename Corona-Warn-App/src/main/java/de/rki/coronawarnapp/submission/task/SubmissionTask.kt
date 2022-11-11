package de.rki.coronawarnapp.submission.task

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.getSupportedCountries
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsTransformer
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType
import de.rki.coronawarnapp.srs.core.SubmissionReporter
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.task.common.Finished
import de.rki.coronawarnapp.task.common.Started
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject
import javax.inject.Provider

@Suppress("LongParameterList")
class SubmissionTask @Inject constructor(
    private val playbook: Playbook,
    private val appConfigProvider: AppConfigProvider,
    private val tekHistoryCalculations: ExposureKeyHistoryCalculations,
    private val tekHistoryStorage: TEKHistoryStorage,
    private val submissionSettings: SubmissionSettings,
    private val autoSubmission: AutoSubmission,
    private val timeStamper: TimeStamper,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    private val checkInsRepository: CheckInRepository,
    private val checkInsTransformer: CheckInsTransformer,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    private val coronaTestRepository: CoronaTestRepository,
    private val submissionReporter: SubmissionReporter,
) : Task<DefaultProgress, SubmissionTask.Result> {

    private val internalProgress = MutableStateFlow<DefaultProgress>(Started)
    override val progress: Flow<DefaultProgress> = internalProgress

    private var isCanceled = false

    private var inBackground = false

    override suspend fun run(arguments: Task.Arguments): Result {
        try {
            Timber.tag(TAG).d("Running with arguments=%s", arguments)
            arguments as Arguments

            if (arguments.checkUserActivity) {
                if (hasRecentUserActivity()) {
                    Timber.tag(TAG).w("User has recently been active in submission, skipping submission.")
                    return Result(state = Result.State.SKIPPED)
                } else {
                    inBackground = true
                }
            }
            val hasGivenConsent = coronaTestRepository.coronaTests.first().any { it.isAdvancedConsentGiven }
            if (!hasGivenConsent) {
                Timber.tag(TAG).w("Consent unavailable. Skipping execution, disabling auto submission.")
                autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)
                return Result(state = Result.State.SKIPPED)
            }

            if (hasExceededRetryAttempts()) {
                throw IllegalStateException("Submission task retry limit exceeded")
            }

            Timber.tag(TAG).i("Proceeding with submission.")
            return performSubmission(arguments.testType)
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "TEK submission failed.")
            throw error
        } finally {
            Timber.i("Finished (isCanceled=$isCanceled).")
            internalProgress.value = Finished
        }
    }

    private fun hasRecentUserActivity(): Boolean {
        val nowUTC = timeStamper.nowUTC
        val lastUserActivity = submissionSettings.lastSubmissionUserActivityUTC.value
        val userInactivity = Duration.between(lastUserActivity, nowUTC)
        Timber.tag(TAG).d(
            "now=%s, lastUserActivity=%s, userInactivity=%dmin",
            nowUTC,
            lastUserActivity,
            userInactivity.toMinutes()
        )

        return userInactivity.toMillis() >= 0 && userInactivity < USER_INACTIVITY_TIMEOUT
    }

    private fun hasExceededRetryAttempts(): Boolean {
        val currentAttempt = submissionSettings.autoSubmissionAttemptsCount.value
        val lastAttemptAt = submissionSettings.autoSubmissionAttemptsLast.value
        Timber.tag(TAG).i(
            "checkRetryAttempts(): submissionAttemptsCount=%d, lastAttemptAt=%s",
            currentAttempt,
            lastAttemptAt
        )

        return if (currentAttempt >= RETRY_ATTEMPTS) {
            Timber.tag(TAG).e("We have execeed our submission attempts, restoring positive test state.")
            autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)
            true
        } else {
            Timber.tag(TAG).d("Within the attempts limit, continuing.")
            submissionSettings.autoSubmissionAttemptsCount.update { it + 1 }
            submissionSettings.autoSubmissionAttemptsLast.update { timeStamper.nowUTC }
            false
        }
    }

    /**
     * Submission could be triggered manually by user where [BaseCoronaTest.Type] parameter is required or
     * automatically by periodic worker where the [BaseCoronaTest.Type] parameter is not required
     */
    private suspend fun performSubmission(testType: BaseCoronaTest.Type?): Result {
        val availableTests = coronaTestRepository.coronaTests.first()
        Timber.tag(TAG).v("Available tests: %s", availableTests)
        val coronaTest = availableTests
            .filter { testType == null || it.type == testType }
            .firstOrNull { it.isSubmissionAllowed }
            ?: return run {
                Timber.tag(TAG).w("Skip submission -> No valid test available to authorize submission")
                Result(state = Result.State.SKIPPED)
            }

        Timber.tag(TAG).d("Submission is authorized by coronaTest=%s", coronaTest)

        val keys: List<TemporaryExposureKey> = try {
            tekHistoryStorage.tekData.first().flatMap { it.keys }
        } catch (e: NoSuchElementException) {
            Timber.tag(TAG).e(e, "tekHistoryStorage access failed, aborting.")
            autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)
            throw e
        }

        val symptoms: Symptoms = submissionSettings.symptoms.value ?: Symptoms.NO_INFO_GIVEN

        val transformedKeys = tekHistoryCalculations.transformToKeyHistoryInExternalFormat(
            keys,
            symptoms
        )
        Timber.tag(TAG).d("Transformed keys with symptoms %s from %s to %s", symptoms, keys, transformedKeys)

        val checkIns = checkInsRepository.completedCheckIns.first().filter {
            it.hasSubmissionConsent && !it.isSubmitted
        }
        val checkInsReport = checkInsTransformer.transform(checkIns, symptoms)

        Timber.tag(TAG).d("Transformed CheckIns from: %s to: %s", checkIns, checkInsReport)

        val authCode: String
        try {
            authCode = playbook.retrieveTan(
                registrationToken = coronaTest.registrationToken,
                authCode = coronaTest.authCode
            )
        } catch (e: Exception) {
            playbook.submitFake()
            throw e
        }

        if (authCode != coronaTest.authCode)
            coronaTestRepository.updateAuthCode(coronaTest.identifier, authCode)
        else Timber.tag(TAG).d("auth code already retrieved, reusing for submission.")

        val submissionData = Playbook.SubmissionData(
            registrationToken = coronaTest.registrationToken,
            authCode = authCode,
            temporaryExposureKeys = transformedKeys,
            consentToFederation = true,
            visitedCountries = appConfigProvider.getAppConfig().getSupportedCountries(),
            unencryptedCheckIns = checkInsReport.unencryptedCheckIns,
            encryptedCheckIns = checkInsReport.encryptedCheckIns,
            submissionType = coronaTest.type.toSubmissionType()
        )

        checkCancel()

        Timber.tag(TAG).d("Submitting %s", submissionData)
        playbook.submit(submissionData)

        analyticsKeySubmissionCollector.reportSubmitted(coronaTest.type)
        if (checkInsReport.encryptedCheckIns.isNotEmpty())
            analyticsKeySubmissionCollector.reportSubmittedWithCheckIns(coronaTest.type)
        if (inBackground)
            analyticsKeySubmissionCollector.reportSubmittedInBackground(coronaTest.type)

        Timber.tag(TAG).d("Submission successful, deleting submission data.")
        tekHistoryStorage.reset()
        submissionSettings.symptoms.update { null }

        Timber.tag(TAG).d("Marking %d submitted CheckIns.", checkIns.size)
        checkIns.forEach { checkIn ->
            try {
                checkInsRepository.updatePostSubmissionFlags(checkIn.id)
            } catch (e: Exception) {
                e.reportProblem(TAG, "CheckIn $checkIn could not be marked as submitted")
            }
        }

        autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)
        setSubmissionFinished(coronaTest.identifier)
        submissionReporter.reportAt(timeStamper.nowUTC)

        return Result(state = Result.State.SUCCESSFUL)
    }

    private suspend fun setSubmissionFinished(identifier: TestIdentifier) {
        Timber.tag(TAG).d("setSubmissionFinished()")
        coronaTestRepository.markAsSubmitted(identifier)

        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    data class Arguments(
        val checkUserActivity: Boolean = false,
        val testType: BaseCoronaTest.Type? = null
    ) : Task.Arguments

    data class Result(
        val state: State
    ) : Task.Result {
        enum class State {
            SUCCESSFUL,
            SKIPPED
        }
    }

    private fun checkCancel() {
        if (isCanceled) throw TaskCancellationException()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    data class Config(
        override val executionTimeout: Duration = Duration.ofMinutes(8),

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.ENQUEUE

    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<SubmissionTask>
    ) : TaskFactory<DefaultProgress, Task.Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config()
        override val taskProvider: () -> Task<DefaultProgress, Task.Result> = {
            taskByDagger.get()
        }
    }

    companion object {
        private const val RETRY_ATTEMPTS = Int.MAX_VALUE
        private val USER_INACTIVITY_TIMEOUT = Duration.ofMinutes(30)
        private const val TAG: String = "SubmissionTask"
    }
}

private fun BaseCoronaTest.Type.toSubmissionType() = when (this) {
    PCR -> SubmissionType.SUBMISSION_TYPE_PCR_TEST
    BaseCoronaTest.Type.RAPID_ANTIGEN -> SubmissionType.SUBMISSION_TYPE_RAPID_TEST
}
