package de.rki.coronawarnapp.presencetracing.warning.download

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.warning.download.server.DiscoveryResult
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceWarningApi
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceWarningServer
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningPackageMetadata
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.util.HourInterval
import de.rki.coronawarnapp.util.TimeAndDateExtensions.deriveHourInterval
import de.rki.coronawarnapp.util.debug.measureTime
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

@Reusable
class TraceWarningPackageSyncTool @Inject constructor(
    private val deviceStorage: DeviceStorage,
    private val server: TraceWarningServer,
    private val repository: TraceWarningRepository,
    private val configProvider: AppConfigProvider,
    private val checkInRepository: CheckInRepository,
    private val downloader: TraceWarningPackageDownloader
) {

    suspend fun syncPackages(mode: TraceWarningApi.Mode): SyncResult {
        Timber.d("syncPackages(mode=$mode)")
        repository.cleanMetadata()
        return measureTime(
            { Timber.tag(TAG).d("syncPackagesForLocation(DE), took %dms", it) },
            { syncPackagesForLocation(mode, LocationCode("DE")) }
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun syncPackagesForLocation(mode: TraceWarningApi.Mode, location: LocationCode): SyncResult {
        Timber.tag(TAG).d("syncTraceWarningPackages(mode=%s,location=%s)", mode, location)

        val oldestCheckIn = checkInRepository.checkInsWithinRetention.first().minByOrNull { it.checkInStart }.also {
            Timber.tag(TAG).d("Our oldest check-in is %s", it)
        }

        if (oldestCheckIn == null) {
            Timber.tag(TAG).w("There were no checkins, cleaning up package metadata, aborting early.")
            val metaDataForLocation = repository.getMetaDataForLocation(location)
            repository.delete(metaDataForLocation)
            return SyncResult(successful = true)
        }

        val downloadConfig: KeyDownloadConfig = configProvider.getAppConfig()

        cleanUpRevokedPackages(downloadConfig)

        val intervalDiscovery: DiscoveryResult = try {
            server.getAvailableIds(mode, location)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to discover available IDs.")
            return SyncResult(successful = false)
        }

        val oldestCheckInInterval = oldestCheckIn.checkInStart.deriveHourInterval()
        val firstRelevantInterval: HourInterval = max(oldestCheckInInterval, intervalDiscovery.oldest)
        Timber.tag(TAG).d(
            "Oldest-server=%s & Oldest-local=%s => first-relevant=%s",
            intervalDiscovery.oldest,
            oldestCheckInInterval,
            firstRelevantInterval
        )

        cleanUpIrrelevantPackages(location, firstRelevantInterval)

        if (firstRelevantInterval > intervalDiscovery.latest) {
            Timber.tag(TAG).d("Known server IDs are older than our newest, aborting early.")
            return SyncResult(successful = true)
        }

        val missingHourIntervals = determineIntervalsToDownload(
            location = location,
            firstRelevant = firstRelevantInterval,
            lastRelevant = intervalDiscovery.latest
        )

        if (missingHourIntervals.isEmpty()) {
            Timber.tag(TAG).d("There are no missing intervals for %s", location)
            return SyncResult(successful = true)
        }

        requireStorageSpaceFor(missingHourIntervals.size)

        val downloadResult = downloader.launchDownloads(
            mode = mode,
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

        return toDelete.also {
            Timber.tag(TAG).d("Cleaned up TraceWarning ids: %s", it)
        }
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

        return toDelete.also {
            Timber.tag(TAG).d("Removed irrelevant packages: %s", it)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun determineIntervalsToDownload(
        location: LocationCode,
        firstRelevant: HourInterval,
        lastRelevant: HourInterval
    ): List<HourInterval> {
        val metadatas = repository.getMetaDataForLocation(location).filter { it.isDownloaded }
        Timber.tag(TAG).d("We already have downloads for %s", metadatas.joinToString(", ") { it.packageId })

        return (firstRelevant..lastRelevant)
            .filter { interval ->
                // If there is no metadata, it's unknown, so we want to download it
                metadatas.none { it.hourInterval == interval }
            }
            .also {
                Timber.tag(TAG).d("Missing intervals for %s are %s", location, it)
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
    ) {
        override fun toString(): String =
            "SyncResult(successful=$successful, newPackages=${newPackages.joinToString(",") { it.packageId }})"
    }

    companion object {
        private const val TAG = "TraceWarningSyncTool"
        private const val APPROX_FILE_SIZE = 22 * 1024L // ~22KB
    }
}
