package de.rki.coronawarnapp.submission.task

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import timber.log.Timber
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
    private val shareTestResultNotificationService: ShareTestResultNotificationService,
    private val testResultAvailableNotificationService: TestResultAvailableNotificationService,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
) : Task<DefaultProgress, SubmissionTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

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

            if (!submissionSettings.hasGivenConsent.value) {
                Timber.tag(TAG).w("Consent unavailable. Skipping execution, disabling auto submission.")
                autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)
                return Result(state = Result.State.SKIPPED)
            }

            if (hasExceededRetryAttempts()) {
                throw IllegalStateException("Submission task retry limit exceeded")
            }

            Timber.tag(TAG).i("Proceeding with submission.")
            return performSubmission()
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "TEK submission failed.")
            throw error
        } finally {
            Timber.i("Finished (isCanceled=$isCanceled).")
            internalProgress.close()
        }
    }

    private fun hasRecentUserActivity(): Boolean {
        val nowUTC = timeStamper.nowUTC
        val lastUserActivity = submissionSettings.lastSubmissionUserActivityUTC.value
        val userInactivity = Duration(lastUserActivity, nowUTC)
        Timber.tag(TAG).d(
            "now=%s, lastUserActivity=%s, userInactivity=%dmin",
            nowUTC,
            lastUserActivity,
            userInactivity.standardMinutes
        )

        return userInactivity.millis >= 0 && userInactivity < USER_INACTIVITY_TIMEOUT
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

    private suspend fun performSubmission(): Result {
        val registrationToken = LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        Timber.tag(TAG).d("Using registrationToken=$registrationToken")

        val keys: List<TemporaryExposureKey> = try {
            tekHistoryStorage.tekData.first().flatMap { it.keys }
        } catch (e: NoSuchElementException) {
            Timber.tag(TAG).e(e, "No TEKs available, aborting.")
            autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)
            throw e
        }

        val symptoms: Symptoms = submissionSettings.symptoms.value ?: Symptoms.NO_INFO_GIVEN

        val transformedKeys = tekHistoryCalculations.transformToKeyHistoryInExternalFormat(
            keys,
            symptoms
        )
        Timber.tag(TAG).d("Transformed keys with symptoms %s from %s to %s", symptoms, keys, transformedKeys)

        val submissionData = Playbook.SubmissionData(
            registrationToken,
            transformedKeys,
            true,
            getSupportedCountries()
        )

        checkCancel()

        Timber.tag(TAG).d("Submitting %s", submissionData)
        playbook.submit(submissionData)

        analyticsKeySubmissionCollector.reportSubmitted()
        if (inBackground) analyticsKeySubmissionCollector.reportSubmittedInBackground()

        Timber.tag(TAG).d("Submission successful, deleting submission data.")
        tekHistoryStorage.clear()
        submissionSettings.symptoms.update { null }

        autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)

        setSubmissionFinished()

        return Result(state = Result.State.SUCCESSFUL)
    }

    private fun setSubmissionFinished() {
        Timber.tag(TAG).d("setSubmissionFinished()")
        BackgroundWorkScheduler.stopWorkScheduler()
        LocalData.numberOfSuccessfulSubmissions(1)
        BackgroundWorkScheduler.startWorkScheduler()

        shareTestResultNotificationService.cancelSharePositiveTestResultNotification()
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    data class Arguments(
        val checkUserActivity: Boolean = false
    ) : Task.Arguments

    data class Result(
        val state: State
    ) : Task.Result {
        enum class State {
            SUCCESSFUL,
            SKIPPED
        }
    }

    private suspend fun getSupportedCountries(): List<String> {
        val countries = appConfigProvider.getAppConfig().supportedCountries
        return when {
            countries.isEmpty() -> {
                Timber.w("Country list was empty, corrected")
                listOf(FALLBACK_COUNTRY)
            }
            else -> countries
        }.also { Timber.i("Supported countries = $it") }
    }

    private fun checkCancel() {
        if (isCanceled) throw TaskCancellationException()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    data class Config(
        override val executionTimeout: Duration = Duration.standardMinutes(8), // TODO unit-test that not > 9 min

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
        private const val FALLBACK_COUNTRY = "DE"
        private const val RETRY_ATTEMPTS = Int.MAX_VALUE
        private val USER_INACTIVITY_TIMEOUT = Duration.standardMinutes(30)
        private const val TAG: String = "SubmissionTask"
    }
}
