package de.rki.coronawarnapp.presencetracing.warning.download

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceTimeWarningApiV1
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceTimeWarningServer
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningPackageMetadata
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.util.HourInterval
import de.rki.coronawarnapp.util.TimeAndDateExtensions.deriveHourInterval
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

@Reusable
class TraceTimeWarningPackageSyncTool @Inject constructor(
    private val deviceStorage: DeviceStorage,
    private val server: TraceTimeWarningServer,
    private val repository: TraceTimeIntervalWarningRepository,
    private val configProvider: AppConfigProvider,
    private val dispatcherProvider: DispatcherProvider,
    private val checkInRepository: CheckInRepository,
    private val downloader: TraceTimeWarningPackageDownloader
) {

    suspend fun syncPackages(): SyncResult {
        return syncPackagesForLocation(LocationCode("DE"))
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun syncPackagesForLocation(location: LocationCode): SyncResult {
        Timber.tag(TAG).d("syncTraceWarningPackages(location=%s)", location)

        val oldestCheckIn = checkInRepository.allCheckIns.first().minByOrNull { it.checkInStart }.also {
            Timber.tag(TAG).d("Our oldest check-in is %s", it)
        }

        if (oldestCheckIn == null) {
            Timber.tag(TAG).w("There were no checkins, cleaning up package metadata, aborting early.")
            val metaDataForLocation = repository.getMetaDataForLocation(location)
            repository.delete(metaDataForLocation)
            return SyncResult(successful = true)
        }

        val downloadConfig: KeyDownloadConfig = configProvider.getAppConfig()

        cleanUpRevokedPackages(downloadConfig).also {
            Timber.tag(TAG).d("Cleaned up TraceWarning ids: %s", it)
        }

        val intervalDiscovery: TraceTimeWarningApiV1.DiscoveryResult = try {
            server.getAvailableIds(location)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to discover available IDs.")
            return SyncResult(successful = false)
        }

        val firstRelevantInterval: HourInterval = max(
            oldestCheckIn.checkInStart.deriveHourInterval(),
            intervalDiscovery.oldest
        )

        cleanUpIrrelevantPackages(location, firstRelevantInterval).also {
            Timber.tag(TAG).d("Removed irrelevant packages: %s", it)
        }

        if (firstRelevantInterval > intervalDiscovery.latest) {
            Timber.tag(TAG).d("Known server IDs are older then ours newest, aborting early.")
            return SyncResult(successful = true)
        }

        val missingHourIntervals = determineIntervalsToDownload(
            location = location,
            firstRelevant = oldestCheckIn.checkInStart.deriveHourInterval(),
            lastRelevant = intervalDiscovery.latest
        )

        if (missingHourIntervals.isEmpty()) {
            Timber.tag(TAG).d("There are no missing intervals for %s", location)
            return SyncResult(successful = true)
        }

        requireStorageSpaceFor(missingHourIntervals.size)

        val downloadResult = downloader.launchDownloads(
            location = location,
            hourIntervals = missingHourIntervals,
            downloadTimeout = downloadConfig.individualDownloadTimeout
        )
        Timber.tag(TAG).i("Download result: %s", downloadResult)

        return SyncResult(
            successful = downloadResult.successful,
            newPackages = downloadResult.newPackages,
        )
    }

    /**
     * Returns true if any of our cached keys were revoked
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun cleanUpRevokedPackages(
        config: KeyDownloadConfig
    ): List<TraceWarningPackageMetadata> {
        val revokedKeyPackages = config.revokedTraceWarningPackages

        if (revokedKeyPackages.isEmpty()) {
            Timber.tag(TAG).d("No revoked key packages to delete.")
            return emptyList()
        }

        val badEtags = revokedKeyPackages.map { it.etag }
        val toDelete = repository.allMetaData.first().filter { badEtags.contains(it.eTag) }
        Timber.tag(TAG).d("Revoked key packages matched %s", toDelete)

        repository.delete(toDelete)

        return toDelete
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun cleanUpIrrelevantPackages(
        location: LocationCode,
        oldestRelevantInterval: HourInterval
    ): List<TraceWarningPackageMetadata> {
        val downloaded = repository.getMetaDataForLocation(location)
        val toDelete = downloaded.filter { it.hourInterval < oldestRelevantInterval }
        Timber.tag(TAG).d("Removing irrelevant ids older than %d: %s", oldestRelevantInterval, toDelete)

        repository.delete(toDelete)

        return toDelete
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun determineIntervalsToDownload(
        location: LocationCode,
        firstRelevant: HourInterval,
        lastRelevant: HourInterval
    ): List<HourInterval> {
        val alreadyProcessed = repository.getMetaDataForLocation(location)

        return (firstRelevant..lastRelevant).filter { interval ->
            val metadata = alreadyProcessed.find {
                it.hourInterval == interval
            } ?: return@filter false

            !metadata.isProcessed
        }
    }

    private suspend fun requireStorageSpaceFor(size: Int): DeviceStorage.CheckResult {
        val requiredBytes: Long = APPROX_FILE_SIZE * size
        Timber.tag(TAG).d("%dB are required for %d files", requiredBytes, size)
        return deviceStorage.requireSpacePrivateStorage(requiredBytes).also {
            Timber.tag(TAG).d("Storage check result: %s", it)
        }
    }

    data class SyncResult(
        val successful: Boolean,
        val newPackages: Collection<TraceWarningPackageMetadata> = emptyList()
    )
}

private const val TAG = "TraceWarningSyncTool"

// TODO check size
// ~22KB
private const val APPROX_FILE_SIZE = 22 * 1024L
