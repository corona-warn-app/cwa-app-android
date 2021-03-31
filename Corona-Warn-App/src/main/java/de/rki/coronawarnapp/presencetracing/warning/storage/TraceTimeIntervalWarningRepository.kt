package de.rki.coronawarnapp.presencetracing.warning.storage

import android.content.Context
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.presencetracing.warning.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.util.HourInterval
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceTimeIntervalWarningRepository @Inject constructor(
    @AppContext private val context: Context,
    private val factory: TraceWarningDatabase.Factory,
    private val timeStamper: TimeStamper
) {
    private val database by lazy { factory.create() }
    private val dao: TraceWarningPackageDao by lazy { database.traceWarningPackageDao() }

    private val storageDir by lazy {
        File(context.cacheDir, "trace_warning_packages").apply {
            if (!exists()) {
                if (mkdirs()) {
                    Timber.d("Trace warning package directory created: %s", this)
                } else {
                    throw IOException("Trace warning package directory creation failed: $this")
                }
            }
        }
    }

    fun getPathForMetaData(metaData: TraceWarningPackageMetadata): File {
        return File(storageDir, metaData.fileName)
    }

    val allWarningPackages: Flow<List<TraceTimeIntervalWarningPackage>> = dao.getAllMetaData().map { metaDatas ->
        metaDatas.map { metaData ->
            TraceWarningPackageContainer(
                id = metaData.packageId,
                packagePath = getPathForMetaData(metaData)
            )
        }
    }

    val allMetaData = dao.getAllMetaData()

    suspend fun createMetadata(location: LocationCode, hourInterval: HourInterval): TraceWarningPackageMetadata {
        val metadata = TraceWarningPackageMetadata(
            location = location,
            hourInterval = hourInterval,
            createdAt = timeStamper.nowUTC
        )
        dao.insert(metadata)
        Timber.tag(TAG).d("Inserted new Metadata: %s", metadata)
        return metadata
    }

    suspend fun getMetaDataForLocation(location: LocationCode): List<TraceWarningPackageMetadata> {
        return dao.getAllMetaDataForLocation(location.identifier)
    }

    suspend fun markDownloadComplete(metadata: TraceWarningPackageMetadata, eTag: String): TraceWarningPackageMetadata {
        Timber.tag(TAG).d("markDownloadComplete(metaData=%s, eTag=%s)", metadata, eTag)
        val update = TraceWarningPackageMetadata.UpdateDownload(
            packageId = metadata.packageId,
            eTag = eTag,
            isDownloaded = true,
            isProcessed = false,
        )
        Timber.tag(TAG).d("Metadata marked as complete: %s", update)
        dao.updateMetaData(update)
        return metadata.copy(
            eTag = eTag,
            isDownloaded = true,
            isProcessed = false,
        )
    }

    suspend fun markPackageProcessed(packageId: WarningPackageId) {
        Timber.tag(TAG).d("markPackageProcessed(packageId=%s)", packageId)
        val update = TraceWarningPackageMetadata.UpdateProcessed(
            packageId = packageId,
            isProcessed = true,
        )
        dao.updateMetaData(update)
    }

    fun deleteFile(path: File) = path.delete()

    suspend fun delete(metadata: List<TraceWarningPackageMetadata>) {
        Timber.tag(TAG).d("delete(metaData=%s)", metadata)
        dao.deleteByIds(metadata.map { it.packageId })
        metadata.map { getPathForMetaData(it) }.forEach {
            if (it.exists()) {
                if (it.delete()) {
                    Timber.tag(TAG).d("Delete TraceWarningPackage file.")
                } else {
                    Timber.tag(TAG).w("Failed to delete TraceWarningPackage file: %s", it)
                }
            }
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        dao.clear()

        if (!storageDir.deleteRecursively()) {
            Timber.tag(TAG).e("Failed to delete all TraceWarningPackage files.")
        }
    }
}

private val TAG = TraceTimeIntervalWarningRepository::class.java.simpleName
