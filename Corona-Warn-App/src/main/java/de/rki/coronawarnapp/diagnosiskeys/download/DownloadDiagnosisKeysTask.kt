package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import de.rki.coronawarnapp.risk.RollbackItem
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskFactory.Config.CollisionBehavior
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.ui.toLazyString
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Provider

class DownloadDiagnosisKeysTask @Inject constructor(
    private val enfClient: ENFClient,
    private val environmentSetup: EnvironmentSetup,
    private val appConfigProvider: AppConfigProvider,
    private val keyPackageSyncTool: KeyPackageSyncTool,
    private val timeStamper: TimeStamper,
    private val settings: DownloadDiagnosisKeysSettings
) : Task<DownloadDiagnosisKeysTask.Progress, Task.Result> {

    private val internalProgress = ConflatedBroadcastChannel<Progress>()
    override val progress: Flow<Progress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Task.Result {
        val rollbackItems = mutableListOf<RollbackItem>()
        try {
            Timber.d("Running with arguments=%s", arguments)
            arguments as Arguments

            /**
             * Handles the case when the ENClient got disabled but the Task is still scheduled
             * in a background job. Also it acts as a failure catch in case the orchestration code did
             * not check in before.
             */
            if (!enfClient.isTracingEnabled.first()) {
                Timber.tag(TAG).w("EN is not enabled, skipping RetrieveDiagnosisKeys")
                return object : Task.Result {}
            }

            throwIfCancelled()
            val currentDate = Date(timeStamper.nowUTC.millis)
            Timber.tag(TAG).d("Using $currentDate as current date in task.")

            throwIfCancelled()

            // RETRIEVE RISK SCORE PARAMETERS
            val exposureConfig: ConfigData = appConfigProvider.getAppConfig()

            internalProgress.send(Progress.ApiSubmissionStarted)
            internalProgress.send(Progress.KeyFilesDownloadStarted)

            val requestedCountries = arguments.requestedCountries
            val keySyncResult = getAvailableKeyFiles(requestedCountries)
            throwIfCancelled()

            if (!exposureConfig.isDeviceTimeCorrect) {
                Timber.tag(TAG).w("Aborting, Device time is incorrect, offset=%s", exposureConfig.localOffset)
                return object : Task.Result {}
            }

            val now = timeStamper.nowUTC

            if (exposureConfig.maxExposureDetectionsPerUTCDay == 0) {
                Timber.tag(TAG).w("Exposure detections are disabled! maxExposureDetectionsPerUTCDay=0")
                return object : Task.Result {}
            }

            val trackedExposureDetections = enfClient.latestTrackedExposureDetection().first()
            val isUpdateToEnfV2 = settings.isUpdateToEnfV2

            Timber.tag(TAG).d("isUpdateToEnfV2: %b", isUpdateToEnfV2)
            if (!isUpdateToEnfV2 && wasLastDetectionPerformedRecently(now, exposureConfig, trackedExposureDetections)) {
                // At most one detection every 6h
                Timber.tag(TAG).i("task aborted, because detection was performed recently")
                return object : Task.Result {}
            }

            if (!isUpdateToEnfV2 && hasRecentDetectionAndNoNewFiles(now, keySyncResult, trackedExposureDetections)) {
                Timber.tag(TAG).i("task aborted, last check was within 24h, and there are no new files")
                return object : Task.Result {}
            }

            val availableKeyFiles = keySyncResult.availableKeys.map { it.path }
            val totalFileSize = availableKeyFiles.fold(0L, { acc, file -> file.length() + acc })

            internalProgress.send(
                Progress.KeyFilesDownloadFinished(
                    availableKeyFiles.size,
                    totalFileSize
                )
            )

            // remember version code of this execution for next time
            settings.updateLastVersionCodeToCurrent()

            Timber.tag(TAG).d("Attempting submission to ENF")
            val isSubmissionSuccessful = enfClient.provideDiagnosisKeys(
                availableKeyFiles,
                exposureConfig.diagnosisKeysDataMapping
            )
            Timber.tag(TAG).d("Diagnosis Keys provided (success=%s)", isSubmissionSuccessful)

            internalProgress.send(Progress.ApiSubmissionFinished)

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

    private fun wasLastDetectionPerformedRecently(
        now: Instant,
        exposureConfig: ExposureDetectionConfig,
        trackedDetections: Collection<TrackedExposureDetection>
    ): Boolean {
        val lastDetection = trackedDetections.maxByOrNull { it.startedAt }
        if (lastDetection == null) {
            Timber.tag(TAG).d("No previous detections exist, don't abort.")
            return false
        }

        if (lastDetection.startedAt.isAfter(now.plus(Duration.standardHours(1)))) {
            Timber.tag(TAG).w("Last detection happened in our future? Don't abort as precaution.")
            return false
        }

        val nextDetectionAt = lastDetection.startedAt.plus(exposureConfig.minTimeBetweenDetections)

        Duration(now, nextDetectionAt).also {
            Timber.tag(TAG).d("Next detection is available in %d min", it.standardMinutes)
        }

        return (now.isBefore(nextDetectionAt)).also {
            if (it) Timber.tag(TAG).w("Aborting. Last detection is recent: %s (now=%s)", lastDetection, now)
        }
    }

    private fun hasRecentDetectionAndNoNewFiles(
        now: Instant,
        keySyncResult: KeyPackageSyncTool.Result,
        trackedDetections: Collection<TrackedExposureDetection>
    ): Boolean {
        // One forced detection every 24h, ignoring the sync results
        val lastSuccessfulDetection = trackedDetections.filter { it.isSuccessful }.maxByOrNull { it.startedAt }
        val nextForcedDetectionAt = lastSuccessfulDetection?.startedAt?.plus(Duration.standardDays(1))

        val hasRecentDetection = nextForcedDetectionAt != null && now.isBefore(nextForcedDetectionAt)

        return (hasRecentDetection && keySyncResult.newKeys.isEmpty()).also {
            if (it) Timber.tag(TAG).w("Aborting. Last detection is recent (<24h) and no new keyfiles.")
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

    private suspend fun getAvailableKeyFiles(
        requestedCountries: List<String>?
    ): KeyPackageSyncTool.Result {
        val wantedLocations = if (environmentSetup.useEuropeKeyPackageFiles) {
            listOf("EUR")
        } else {
            requestedCountries ?: appConfigProvider.getAppConfig().supportedCountries
        }.map { LocationCode(it) }

        return keyPackageSyncTool.syncKeyFiles(wantedLocations)
    }

    private fun throwIfCancelled() {
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
        val requestedCountries: List<String>? = null
    ) : Task.Arguments

    data class Config(
        override val executionTimeout: Duration = Duration.standardMinutes(8), // TODO unit-test that not > 9 min

        override val collisionBehavior: CollisionBehavior = CollisionBehavior.SKIP_IF_SIBLING_RUNNING

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
