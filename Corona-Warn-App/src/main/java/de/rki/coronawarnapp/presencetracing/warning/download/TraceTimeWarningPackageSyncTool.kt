package de.rki.coronawarnapp.presencetracing.warning.download

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceWarningPackageMetadataEntity
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.io.File
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

@Reusable
class TraceTimeWarningPackageSyncTool @Inject constructor(
    private val deviceStorage: DeviceStorage,
    private val gateway: TraceTimeWarningGateway,
    private val repository: TraceTimeIntervalWarningRepository,
    private val configProvider: AppConfigProvider,
    private val dispatcherProvider: DispatcherProvider,
    private val checkInRepository: CheckInRepository
) {

    private val successfulEmptyResult: SyncResult
        get() = SyncResult(successful = true)

    private val failureResult: SyncResult
        get() = SyncResult(successful = false)

    internal suspend fun syncTraceWarningPackages(): SyncResult {

        val oldestCheckIn = checkInRepository.allCheckIns.first().minByOrNull {
            it.checkInStart
        }
        if (oldestCheckIn == null) {
            repository.deleteAllPackages()
            return successfulEmptyResult
        }

        val downloadConfig: KeyDownloadConfig = configProvider.getAppConfig()
        // TODO
        // Clean up Revoked Packages: if the Configuration Parameter .keyDownloadParameters.revokedTraceWarningPackages
        // contains an ETag that matches any of the ETags in the Database Table for TraceWarningPackageMetadata,
        // the corresponding record is removed from the database table.
        // deleteRevokedPackages()

        val response = gateway.getAvailableWarningPackageIds()
        if (!response.isSuccessful) {
            return failureResult
        }
        val packageIds = response.body() ?: return successfulEmptyResult

        val firstRelevantPackage = max(oldestCheckIn.checkInStart.millis / 3600000, packageIds.oldest)
        val downloadedPackages = getDownloadedPackageMetadata()

        val stalePackages = findStalePackages(downloadedPackages, firstRelevantPackage)
        repository.deleteStalePackage(stalePackages)

        if (firstRelevantPackage > packageIds.latest) {
            successfulEmptyResult
        }

        val missingPackageIds = findMissingPackageIds(downloadedPackages, firstRelevantPackage, packageIds.latest)
        if (missingPackageIds.isEmpty()) {
            successfulEmptyResult
        }

        requireStorageSpaceFor(missingPackageIds.size)

        val downloads = launchDownloads(
            packageIds = missingPackageIds,
            downloadTimeoutMillis = downloadConfig.individualDownloadTimeout.millis
        )

        val downloadedPackageIds = downloads.awaitAll().filterNotNull().also {
            Timber.tag(TAG).v("Downloaded keyfile: %s", it.joinToString("\n"))
        }
        Timber.tag(TAG).i("Download success: ${downloadedPackageIds.size}/${downloads.size}")

        return SyncResult(
            successful = downloads.size == downloadedPackageIds.size,
            newPackages = downloadedPackageIds
        )
    }

    private fun findStalePackages(
        downloaded: List<TraceWarningPackageMetadataEntity>,
        oldestRelevantId: WarningPackageId
    )
    : List<WarningPackageId> =
        downloaded.filter {
            it.packageId < oldestRelevantId
        }.map {
            it.packageId
        }

    private fun findMissingPackageIds(
        downloaded: List<TraceWarningPackageMetadataEntity>,
        firstRelevantId: WarningPackageId,
        lastRelevantId: WarningPackageId)
    : List<WarningPackageId> {
        return (firstRelevantId..lastRelevantId).filter { packageId ->
            val metadata = downloaded.find { it.packageId == packageId}?: return@filter false
            // TODO what about ongoing downloads?
            !(metadata.isDownloadComplete && File(metadata.absolutePath).exists())
        }
    }

    private suspend fun requireStorageSpaceFor(size: Int): DeviceStorage.CheckResult {
        val requiredBytes: Long = APPROX_FILE_SIZE * size
        Timber.tag(TAG).d("%dB are required for %d files", requiredBytes, size)
        return deviceStorage.requireSpacePrivateStorage(requiredBytes).also {
            Timber.tag(TAG).d("Storage check result: %s", it)
        }
    }

    private fun getDownloadedPackageMetadata(): List<TraceWarningPackageMetadataEntity> =
        repository.packageMetadataEntities
        .filter { metadata ->
            val complete = metadata.isDownloadComplete
            val exists = File(metadata.absolutePath).exists()
            if (complete && !exists) {
                Timber.tag(TAG).v("Incomplete download, will overwrite: %s", metadata)
            }
            // We overwrite not completed ones
            complete && exists
        }

    private suspend fun launchDownloads(
        packageIds: List<WarningPackageId>,
        downloadTimeoutMillis: Long
    ): Collection<Deferred<WarningPackageId?>> {
        val launcher: CoroutineScope.(WarningPackageId) -> Deferred<WarningPackageId?> =
            { packageId ->
                async {
                    val metadata = repository.createMetadata(packageId, LocationCode(Locale.GERMANY.country))
                    val file = File(metadata.absolutePath)
                    downloadTraceWarningPackage(packageId, file, downloadTimeoutMillis)
                }
            }

        Timber.tag(TAG).d("Launching %d downloads.", packageIds.size)

        return packageIds.map { warningPackageId ->
            withContext(context = dispatcherProvider.IO) {
                launcher(warningPackageId)
            }
        }
    }

    private suspend fun downloadTraceWarningPackage(
        warningPackageId: WarningPackageId,
        saveToFile: File,
        downloadTimeoutMillis: Long
    ): Long? {
        try {

            val downloadInfo = withTimeout(downloadTimeoutMillis) {
                gateway.downloadWarningPackageFile(
                    warningPackageId = warningPackageId,
                    saveTo = saveToFile
                )
            }
            Timber.tag(TAG).v("Download finished: warningPackageId %s -> %s", warningPackageId, saveToFile)

            //TODO
            //Verify Signature: for each downloaded TraceWarningPackage that is not empty
            // (i.e. not a zip file, cwa-empty-pkg header set), the signature shall be verified.
            // If signature verification fails, the TraceWarningPackage is discarded and not considered as
            // successfully downloaded.

            val eTag = requireNotNull(downloadInfo.etag) { "Server provided no ETAG!" }

            repository.markDownloadComplete(warningPackageId, eTag, saveToFile)
            return warningPackageId
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Download failed: %s", warningPackageId)
            repository.deleteFile(saveToFile)
            return null
        }
    }

    /**
     * Returns true if any of our cached keys were revoked
     */
    internal fun deleteRevokedPackages(
        revokedKeyPackages: Collection<KeyDownloadConfig.RevokedKeyPackage>
    ): Boolean {
        if (revokedKeyPackages.isEmpty()) {
            Timber.tag(TAG).d("No revoked key packages to delete.")
            return false
        }

        val badEtags = revokedKeyPackages.map { it.etag }
        val toDelete = repository.packageMetadataEntities.filter { badEtags.contains(it.eTag) }

        return if (toDelete.isEmpty()) {
            Timber.tag(TAG).d("No local cached keys matched the revoked ones.")
            false
        } else {
            Timber.tag(TAG).w("Deleting revoked cached keys: %s", toDelete.joinToString("\n"))
            repository.deleteStalePackage(toDelete.map { it.packageId })
            true
        }
    }

    data class SyncResult(
        val successful: Boolean,
        val newPackages: List<WarningPackageId> = emptyList()
    )

}

private const val TAG = "TraceWarningSyncTool"

// TODO check size
// ~22KB
private const val APPROX_FILE_SIZE = 22 * 1024L

