package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.RollbackItem
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import timber.log.Timber
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider

class DownloadDiagnosisKeysTask @Inject constructor(
    private val enfClient: ENFClient,
    private val environmentSetup: EnvironmentSetup,
    private val appConfigProvider: AppConfigProvider,
    private val keyFileDownloader: KeyFileDownloader,
    private val timeStamper: TimeStamper
) : Task<DownloadDiagnosisKeysTask.Progress, Task.Result> {

    private val internalProgress = ConflatedBroadcastChannel<Progress>()
    override val progress: Flow<Progress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Task.Result {
        val rollbackItems = mutableListOf<RollbackItem>()
        try {
            Timber.d("Running with arguments=%s", arguments)
            arguments as Arguments

            if (arguments.withConstraints) {
                if (!noKeysFetchedToday()) return object : Task.Result {}
            }

            /**
             * Handles the case when the ENClient got disabled but the Task is still scheduled
             * in a background job. Also it acts as a failure catch in case the orchestration code did
             * not check in before.
             */
            if (!InternalExposureNotificationClient.asyncIsEnabled()) {
                Timber.tag(TAG).w("EN is not enabled, skipping RetrieveDiagnosisKeys")
                return object : Task.Result {}
            }

            checkCancel()
            val currentDate = Date(timeStamper.nowUTC.millis)
            Timber.tag(TAG).d("Using $currentDate as current date in task.")

            /****************************************************
             * RETRIEVE TOKEN
             ****************************************************/
            val token = retrieveToken(rollbackItems)
            checkCancel()

            // RETRIEVE RISK SCORE PARAMETERS
            val exposureConfiguration = appConfigProvider.getAppConfig().exposureDetectionConfiguration

            internalProgress.send(Progress.ApiSubmissionStarted)
            internalProgress.send(Progress.KeyFilesDownloadStarted)

            val requestedCountries = arguments.requestedCountries
            val availableKeyFiles = getAvailableKeyFiles(requestedCountries)
            checkCancel()

            val totalFileSize = availableKeyFiles.fold(0L, { acc, file ->
                file.length() + acc
            })

            internalProgress.send(
                Progress.KeyFilesDownloadFinished(
                    availableKeyFiles.size,
                    totalFileSize
                )
            )

            Timber.tag(TAG).d("Attempting submission to ENF")
            val isSubmissionSuccessful = enfClient.provideDiagnosisKeys(
                keyFiles = availableKeyFiles,
                configuration = exposureConfiguration,
                token = token
            )
            Timber.tag(TAG).d("Diagnosis Keys provided (success=%s, token=%s)", isSubmissionSuccessful, token)

            internalProgress.send(Progress.ApiSubmissionFinished)
            checkCancel()

            if (isSubmissionSuccessful) {
                saveTimestamp(currentDate, rollbackItems)
            }

            return object : Task.Result {}
        } catch (error: Exception) {
            Timber.tag(TAG).e(error)

            rollback(rollbackItems)

            throw error
        } finally {
            Timber.i("Finished (isCanceled=$isCanceled).")
            internalProgress.close()
        }
    }

    private fun saveTimestamp(
        currentDate: Date,
        rollbackItems: MutableList<RollbackItem>
    ) {
        val lastFetchDateForRollback = LocalData.lastTimeDiagnosisKeysFromServerFetch()
        rollbackItems.add {
            LocalData.lastTimeDiagnosisKeysFromServerFetch(lastFetchDateForRollback)
        }
        Timber.tag(TAG).d("dateUpdate(currentDate=%s)", currentDate)
        LocalData.lastTimeDiagnosisKeysFromServerFetch(currentDate)
    }

    private fun retrieveToken(rollbackItems: MutableList<RollbackItem>): String {
        val googleAPITokenForRollback = LocalData.googleApiToken()
        rollbackItems.add {
            LocalData.googleApiToken(googleAPITokenForRollback)
        }
        return UUID.randomUUID().toString().also {
            LocalData.googleApiToken(it)
        }
    }

    private fun noKeysFetchedToday(): Boolean {
        val currentDate = DateTime(timeStamper.nowUTC, DateTimeZone.UTC)
        val lastFetch = DateTime(
            LocalData.lastTimeDiagnosisKeysFromServerFetch(),
            DateTimeZone.UTC
        )
        return (LocalData.lastTimeDiagnosisKeysFromServerFetch() == null ||
            currentDate.withTimeAtStartOfDay() != lastFetch.withTimeAtStartOfDay()).also {
            if (it) {
                Timber.tag(TAG)
                    .d("No keys fetched today yet (last=%s, now=%s)", lastFetch, currentDate)
                BackgroundWorkHelper.sendDebugNotification(
                    "Start Task",
                    "No keys fetched today yet \n${DateTime.now()}\nUTC: $currentDate"
                )
            }
        }
    }

    private fun rollback(rollbackItems: MutableList<RollbackItem>) {
        try {
            Timber.tag(TAG).d("Initiate Rollback")
            for (rollbackItem: RollbackItem in rollbackItems) rollbackItem.invoke()
        } catch (rollbackException: Exception) {
            Timber.tag(TAG).e(rollbackException, "Rollback failed.")
        }
    }

    private suspend fun getAvailableKeyFiles(requestedCountries: List<String>?): List<File> {
        val availableKeyFiles =
            keyFileDownloader.asyncFetchKeyFiles(if (environmentSetup.useEuropeKeyPackageFiles) {
                listOf("EUR")
            } else {
                requestedCountries
                    ?: appConfigProvider.getAppConfig().supportedCountries
            }.map { LocationCode(it) })

        if (availableKeyFiles.isEmpty()) {
            Timber.tag(TAG).w("No keyfiles were available!")
        }
        return availableKeyFiles
    }

    private fun checkCancel() {
        if (isCanceled) throw TaskCancellationException()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    sealed class Progress : Task.Progress {
        object ApiSubmissionStarted : Progress()
        object ApiSubmissionFinished : Progress()

        object KeyFilesDownloadStarted : Progress()
        data class KeyFilesDownloadFinished(val keyCount: Int, val fileSize: Long) : Progress()

        override val primaryMessage = this::class.java.simpleName.toLazyString()
    }

    class Arguments(
        val requestedCountries: List<String>? = null,
        val withConstraints: Boolean = false
    ) : Task.Arguments

    data class Config(
        @Suppress("MagicNumber")
        override val executionTimeout: Duration = Duration.standardMinutes(8), // TODO unit-test that not > 9 min

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.ENQUEUE

    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<DownloadDiagnosisKeysTask>,
        private val appConfigProvider: AppConfigProvider
    ) : TaskFactory<Progress, Task.Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config(
            executionTimeout = appConfigProvider.getAppConfig().overallDownloadTimeout
        )

        override val taskProvider: () -> Task<Progress, Task.Result> = {
            taskByDagger.get()
        }
    }

    companion object {
        private val TAG: String? = DownloadDiagnosisKeysTask::class.simpleName
    }
}
