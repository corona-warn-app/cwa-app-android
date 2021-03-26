package de.rki.coronawarnapp.presencetracing.warning.download

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeWarningPackageMetadata
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageIds
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
import javax.inject.Inject

private const val TAG = "TraceWarningSyncTool"
@Reusable
class TraceTimeWarningPackageSyncTool @Inject constructor(
    private val deviceStorage: DeviceStorage,
    private val gateway: TraceTimeWarningGateway,
    private val repository: TraceTimeIntervalWarningRepository,
    private val configProvider: AppConfigProvider,
    private val dispatcherProvider: DispatcherProvider
) {

    internal suspend fun syncTraceWarningPackages(): SyncResult {

        // TODO
        // 1. Check for check-ins: if the Database Table for CheckIns is empty, download is aborted without any errors.
        // In that case, all records from the Database Table for TraceWarningPackageMetadata are removed

        val downloadConfig: KeyDownloadConfig = configProvider.getAppConfig()

        // TODO
        // Clean up Revoked Packages: if the Configuration Parameter .keyDownloadParameters.revokedTraceWarningPackages
        // contains an ETag that matches any of the ETags in the Database Table for TraceWarningPackageMetadata,
        // the corresponding record is removed from the database table.

        // get available packages
        val response = gateway.getAvailableWarningPackageIds()

        if (!response.isSuccessful) {
            return SyncResult(successful = false, newPackages = emptyList())
        }
        val packageIds = response.body()

        val downloadedPackages = getDownloadedPackageMetadata()

        // delete stale packages
        val stalePackages = findStalePackages(downloadedPackages, packageIds)
        if (stalePackages.isNotEmpty()) {
            repository.deleteStalePackage(stalePackages)
        }

        // download missing packages
        val missingPackageIds = findMissingPackageIds(downloadedPackages, packageIds)
        if (missingPackageIds.isEmpty()) {
            return SyncResult(successful = true, newPackages = emptyList())
        }

        requireStorageSpaceFor(missingPackageIds)

        val downloads = launchDownloads(missingPackageIds, downloadConfig)

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
        downloaded: List<TraceTimeWarningPackageMetadata>,
        available: WarningPackageIds
    )
    : List<WarningPackageId> =
        downloaded.filter {
            it.packageId < available.oldest
        }.map {
            it.packageId
        }

    private fun findMissingPackageIds(
        downloaded: List<TraceTimeWarningPackageMetadata>,
        available: WarningPackageIds )
    : List<WarningPackageId> {
        return (available.oldest..available.latest).filter { packageId ->
            val metadata = downloaded.find { it.packageId == packageId}?: return@filter false
            // TODO what about ongoing downloads?
            !(metadata.isDownloadComplete &&
                metadata.absolutePath != null &&
                File(metadata.absolutePath).exists())
        }
    }

    private suspend fun requireStorageSpaceFor(data: List<WarningPackageId>): DeviceStorage.CheckResult {
        val requiredBytes: Long = 0L // TODO calculate
        Timber.tag(TAG).d("%dB are required for %s", requiredBytes, data)
        return deviceStorage.requireSpacePrivateStorage(requiredBytes).also {
            Timber.tag(TAG).d("Storage check result: %s", it)
        }
    }

    private suspend fun getDownloadedPackageMetadata(): List<TraceTimeWarningPackageMetadata> =
        repository.allPackageInfos.first()
        .filter { metadata ->
            val complete = metadata.isDownloadComplete
            val exists = metadata.absolutePath != null && File(metadata.absolutePath).exists()
            if (complete && !exists) {
                Timber.tag(TAG).v("Incomplete download, will overwrite: %s", metadata)
            }
            // We overwrite not completed ones
            complete && exists
        }

    private suspend fun launchDownloads(
        packageIds: List<WarningPackageId>,
        downloadConfig: KeyDownloadConfig
    ): Collection<Deferred<WarningPackageId?>> {
        val launcher: CoroutineScope.(WarningPackageId) -> Deferred<WarningPackageId?> =
            { packageId ->
                async {
                    val metadata = repository.createMetadata(packageId)
                    val file = repository.getFile(packageId)
                    downloadWarningPackageKeyFile(packageId, file, downloadConfig)

                }
            }

        Timber.tag(TAG).d("Launching %d downloads.", packageIds.size)

        return packageIds.map { warningPackageId ->
            withContext(context = dispatcherProvider.IO) {
                launcher(warningPackageId)
            }
        }
    }

    suspend fun downloadWarningPackageKeyFile(
        warningPackageId: WarningPackageId,
        path: File,
        downloadConfig: KeyDownloadConfig
    ): Long? {
        try {

            val downloadInfo = withTimeout(downloadConfig.individualDownloadTimeout.millis) {
                gateway.downloadWarningPackageFile(
                    warningPackageId = warningPackageId,
                    saveTo = path
                )
            }
            Timber.tag(TAG).v("Download finished: warningPackageId %s -> %s", warningPackageId, path)

            val etag = requireNotNull(downloadInfo.etag) { "Server provided no ETAG!" }
            repository.markDownloadComplete(warningPackageId, etag, path)
            return warningPackageId
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Download failed: %s", warningPackageId)
            repository.deleteFile(path)
            return null
        }
    }

    data class SyncResult(
        val successful: Boolean = true,
        val newPackages: List<WarningPackageId> = emptyList()
    )
}

