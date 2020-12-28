package de.rki.coronawarnapp.submission

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.storage.LocalData
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
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class SubmissionTask @Inject constructor(
    private val playbook: Playbook,
    private val appConfigProvider: AppConfigProvider,
    private val tekHistoryCalculations: ExposureKeyHistoryCalculations,
    private val tekHistoryStorage: TEKHistoryStorage,
    private val submissionSettings: SubmissionSettings,
    private val testResultNotificationService: TestResultNotificationService,
    private val timeStamper: TimeStamper
) : Task<DefaultProgress, SubmissionTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.tag(TAG).d("Running with arguments=%s", arguments)

        if (hasRecentUserActivity()) {
            Timber.tag(TAG).w("User has recently been active in submission, skipping submission.")
            Result(state = Result.State.SKIPPED)
        } else {
            Timber.tag(TAG).i("User was not recently active in submission, proceeding with submission.")
            performSubmission()
        }
    } catch (error: Exception) {
        Timber.tag(TAG).e(error, "TEK submission failed.")
        throw error
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.close()
    }

    private fun hasRecentUserActivity(): Boolean {
        val nowUTC = timeStamper.nowUTC
        val lastUserActivity = submissionSettings.lastSubmissionUserActivityUTC.value
        val userInactivity = Duration(lastUserActivity, nowUTC)
        Timber.tag(TAG).d("now=%s, lastUserActivity=%s, userInactivity=%s", nowUTC, lastUserActivity, userInactivity)

        return userInactivity < INACTIVITY_TIMEOUT
    }

    private suspend fun performSubmission(): Result {
        val registrationToken = LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        Timber.tag(TAG).d("Using registrationToken=$registrationToken")

        val keys: List<TemporaryExposureKey> = tekHistoryStorage.tekData.first().flatMap { it.keys }
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

        Timber.tag(TAG).d("Submission successful, deleting submission data.")
        tekHistoryStorage.clear()
        submissionSettings.symptoms.update { null }
        submissionSettings.lastSubmissionUserActivityUTC.update { Instant.EPOCH }

        // TODO re-evaluate the necessity of this behavior
        BackgroundWorkScheduler.stopWorkScheduler()
        LocalData.numberOfSuccessfulSubmissions(1)

        testResultNotificationService.cancelPositiveTestResultNotification()

        return Result(state = Result.State.SUCCESSFUL)
    }

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
        private val INACTIVITY_TIMEOUT = Duration.standardMinutes(15)
        private const val TAG: String = "SubmissionTask"
    }
}
