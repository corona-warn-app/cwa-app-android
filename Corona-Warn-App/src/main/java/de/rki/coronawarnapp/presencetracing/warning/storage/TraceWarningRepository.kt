package de.rki.coronawarnapp.presencetracing.warning.storage

import android.content.Context
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.util.HourInterval
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceWarningRepository @Inject constructor(
    @AppContext private val context: Context,
    private val factory: TraceWarningDatabase.Factory,
    private val timeStamper: TimeStamper
) : Resettable {
    private val database by lazy { factory.create() }
    private val dao: TraceWarningPackageDao by lazy { database.traceWarningPackageDao() }

    private val storageDir by lazy {
        File(context.cacheDir, "trace_warning_packages").apply {
            if (!exists()) {
                if (mkdirs()) {
                    Timber.tag(TAG).d("Trace warning package directory created: %s", this)
                } else {
                    throw IOException("Trace warning package directory creation failed: $this")
                }
            }
        }
    }

    val unprocessedWarningPackages: Flow<List<TraceWarningPackage>> = dao.getAllMetaData()
        .map { metadatas ->
            Timber.tag(TAG).v("Known packages: ${metadatas.size}")
            metadatas.filter { !it.isProcessed }
        }
        .map { unprocessed ->
            Timber.tag(TAG).v("Unprocessed packages: ${unprocessed.size}")
            unprocessed.filter { !it.isEmptyPkg }
        }
        .map { metaDatas ->
            Timber.tag(TAG).v("There are ${metaDatas.size} unprocessed non-empty warning packages.")
            metaDatas.map { metaData ->
                TraceWarningPackageContainer(
                    packageId = metaData.packageId,
                    packagePath = getPathForMetaData(metaData)
                )
            }
        }

    fun getPathForMetaData(metaData: TraceWarningPackageMetadata): File {
        return File(storageDir, metaData.fileName)
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

    suspend fun markDownloadComplete(
        metadata: TraceWarningPackageMetadata,
        eTag: String,
        isEmptyPkg: Boolean
    ): TraceWarningPackageMetadata {
        Timber.tag(TAG).d("markDownloadComplete(metaData=%s, eTag=%s)", metadata, eTag)
        val update = TraceWarningPackageMetadata.UpdateDownload(
            packageId = metadata.packageId,
            eTag = eTag,
            isDownloaded = true,
            isProcessed = false,
            isEmptyPkg = isEmptyPkg,
        )
        Timber.tag(TAG).d("Metadata marked as complete: %s", update)
        dao.updateMetaData(update)
        return metadata.copy(
            eTag = eTag,
            isDownloaded = true,
            isProcessed = false,
            isEmptyPkg = isEmptyPkg,
        )
    }

    suspend fun markPackagesProcessed(packageIds: List<WarningPackageId>) {
        Timber.tag(TAG).v("markPackagesProcessed(packageIds=%s)", packageIds)

        packageIds.forEach { packageId ->
            Timber.tag(TAG).d("markPackageProcessed(packageId=%s)", packageId)
            val update = TraceWarningPackageMetadata.UpdateProcessed(
                packageId = packageId,
                isProcessed = true,
            )
            dao.updateMetaData(update)

            dao.get(packageId)?.also {
                val file = getPathForMetaData(it)
                if (file.delete()) {
                    Timber.tag(TAG).v("Deleted processed file: %s", file)
                }
            }
        }
    }

    suspend fun delete(metadata: List<TraceWarningPackageMetadata>) {
        Timber.tag(TAG).d("delete(metaData=%s)", metadata.map { it.packageId })
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

    override suspend fun reset() {
        Timber.tag(TAG).d("reset()")
        dao.clear()

        if (!storageDir.deleteRecursively()) {
            Timber.tag(TAG).e("Failed to delete all TraceWarningPackage files.")
        }
    }

    suspend fun cleanMetadata() {
        Timber.tag(TAG).d("cleanMetadata()")
        val allMetadata = allMetaData.first()

        // Lost files, system deleted cache?
        run {
            val shouldHaveFile = allMetadata.filter { it.isDownloaded && !it.isProcessed && !it.isEmptyPkg }
            val toDelete = shouldHaveFile.filter { !getPathForMetaData(it).exists() }
            if (toDelete.isNotEmpty()) {
                Timber.tag(TAG).w("%d Metadata items lost their file", toDelete.size)
            }
            delete(toDelete)
        }

        // Shouldn't have a file, but has one? Gremlins?
        run {
            val shouldNotHaveFile = allMetadata.filter { it.isDownloaded && (it.isProcessed || it.isEmptyPkg) }
            val toDelete = shouldNotHaveFile.filter { getPathForMetaData(it).exists() }
            if (toDelete.isNotEmpty()) {
                Timber.tag(TAG).w("%d Metadata items have unexpected files", toDelete.size)
            }
            delete(toDelete)
        }

        // File without owner?
        storageDir.listFiles()?.forEach { file ->
            val orphan = allMetadata.none { getPathForMetaData(it) == file }

            if (orphan && file.delete()) {
                Timber.tag(TAG).w("Deleted orphaned file: %s", file)
            }
        }
    }

    companion object {
        private val TAG = TraceWarningRepository::class.java.simpleName
    }
}
