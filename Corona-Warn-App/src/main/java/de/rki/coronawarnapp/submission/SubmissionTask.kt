package de.rki.coronawarnapp.submission

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class SubmissionTask @Inject constructor(
    private val playbook: Playbook,
    private val appConfigProvider: AppConfigProvider,
    private val exposureKeyHistoryCalculations: ExposureKeyHistoryCalculations
) : Task<DefaultProgress, Task.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments) = try {
        Timber.d("Running with arguments=%s", arguments)
        arguments as Arguments

        Playbook.SubmissionData(
            arguments.registrationToken,
            arguments.getHistory(),
            true,
            getSupportedCountries()
        )
            .also { checkCancel() }
            .let { playbook.submit(it) }

        SubmissionService.submissionSuccessful()

        object : Task.Result {}
    } catch (error: Exception) {
        Timber.tag(TAG).e(error)
        throw error
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.close()
    }

    private fun Arguments.getHistory(): List<TemporaryExposureKeyExportOuterClass.TemporaryExposureKey> =
        exposureKeyHistoryCalculations.transformToKeyHistoryInExternalFormat(
            keys,
            symptoms
        )

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

    class Arguments(
        val registrationToken: String,
        val keys: List<TemporaryExposureKey>,
        val symptoms: Symptoms
    ) : Task.Arguments

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
        private val TAG: String? = SubmissionTask::class.simpleName
    }
}
