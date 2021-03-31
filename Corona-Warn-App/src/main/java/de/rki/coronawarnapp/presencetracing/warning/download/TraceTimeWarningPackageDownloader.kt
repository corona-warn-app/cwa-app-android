package de.rki.coronawarnapp.presencetracing.warning.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceTimeWarningServer
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningPackageMetadata
import de.rki.coronawarnapp.util.HourInterval
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.SignatureValidation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TraceTimeWarningPackageDownloader @Inject constructor(
    private val repository: TraceTimeIntervalWarningRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val server: TraceTimeWarningServer,
    private val signatureValidation: SignatureValidation,
) {

    data class DownloadResult(
        val successful: Boolean,
        val newPackages: Collection<TraceWarningPackageMetadata>
    )

    suspend fun launchDownloads(
        location: LocationCode,
        hourIntervals: List<HourInterval>,
        downloadTimeout: Duration
    ): DownloadResult {
        val launcher: CoroutineScope.(HourInterval) -> Deferred<TraceWarningPackageMetadata?> = { hourInterval ->
            async {
                val metadata = repository.createMetadata(location, hourInterval)
                withTimeout(downloadTimeout.millis) {
                    downloadPackageForMetaData(metadata)
                }
            }
        }

        Timber.tag(TAG).d("Launching %d downloads.", hourIntervals.size)

        val launchedDownloads: Collection<Deferred<TraceWarningPackageMetadata?>> =
            hourIntervals.map { warningPackageId ->
                withContext(context = dispatcherProvider.IO) {
                    launcher(warningPackageId)
                }
            }

        val successfulDownloads = launchedDownloads.awaitAll()
            .filterNotNull()
            .also {
                Timber.tag(TAG).v("Downloaded keyfile: %s", it.joinToString("\n"))
            }
        Timber.tag(TAG).i("Download success: ${successfulDownloads.size}/${launchedDownloads.size}")

        return DownloadResult(
            successful = launchedDownloads.size == successfulDownloads.size,
            newPackages = successfulDownloads
        )
    }

    private suspend fun downloadPackageForMetaData(
        metaData: TraceWarningPackageMetadata
    ): TraceWarningPackageMetadata? {
        val saveTo = repository.getPathForMetaData(metaData)
        try {
            val downloadInfo = server.downloadPackage(
                location = metaData.location,
                hourInterval = metaData.hourInterval
            )

            val binary = if (downloadInfo.isEmptyPkg) {
                Timber.tag(TAG).w("Empty package for %s", metaData)
                byteArrayOf()
            } else {
                val fileMap = downloadInfo.readBody().unzip().readIntoMap()
                getValidatedBinary(metaData, fileMap)
            }

            if (saveTo.exists()) {
                Timber.tag(TAG).w("File existed, overwriting: %s", saveTo)
                if (saveTo.delete()) {
                    Timber.tag(TAG).e("%s exists, but can't be deleted.", saveTo)
                }
            }

            saveTo.writeBytes(binary)
            Timber.tag(TAG).v("%d bytes written to %s.", binary.size, saveTo)


            Timber.tag(TAG).v("Download finished: %s -> %s", metaData, downloadInfo)

            val eTag = requireNotNull(downloadInfo.etag) { "Server provided no ETAG!" }

            return repository.markDownloadComplete(metaData, eTag)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Download failed: %s", metaData)
            repository.deleteFile(saveTo)
            return null
        }
    }

    private fun getValidatedBinary(
        metaData: TraceWarningPackageMetadata,
        fileMap: Map<String, ByteArray>
    ): ByteArray {
        val signature = fileMap[EXPORT_SIGNATURE_NAME] ?: throw TraceTimeWarningPackageValidationException(
            message = "Signature was null for ${metaData.packageId}(${metaData.eTag})."
        )

        val binary = fileMap[EXPORT_BINARY_NAME] ?: throw TraceTimeWarningPackageValidationException(
            message = "Binary was null for ${metaData.packageId}(${metaData.eTag})."
        )

        val hasValidSignature = signatureValidation.hasValidSignature(
            binary,
            SignatureValidation.parseTEKStyleSignature(signature)
        )

        if (!hasValidSignature) {
            throw TraceTimeWarningPackageValidationException(
                message = "Signature didn't match for ${metaData.packageId}(${metaData.eTag})."
            )
        }

        return binary
    }
}

private const val TAG = "TraceWarningDownloader"
private const val EXPORT_BINARY_NAME = "export.bin"
private const val EXPORT_SIGNATURE_NAME = "export.sig"
