package de.rki.coronawarnapp.storage

import android.annotation.TargetApi
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.text.format.Formatter
import dagger.Reusable
import de.rki.coronawarnapp.util.ApiLevel
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.storage.StatsFsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@Reusable
class DeviceStorage @Inject constructor(
    @AppContext private val context: Context,
    private val apiLevel: ApiLevel,
    private val statsFsProvider: StatsFsProvider
) {

    private val privateStorage = context.filesDir
    private val storageManager by lazy { context.getSystemService(Context.STORAGE_SERVICE) as StorageManager }

    @TargetApi(Build.VERSION_CODES.O)
    private fun requestStorageAPI26Plus(targetPath: File, requiredBytes: Long = -1L): CheckResult {
        Timber.tag(TAG).v(
            "requestStorageAPI26Plus(path=%s, requiredBytes=%d)", targetPath, requiredBytes

        )
        val statsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

        val storageUUID: UUID = storageManager.getUuidForPath(targetPath)

        val totalBytes = statsManager.getTotalBytes(storageUUID)
        var availableBytes = statsManager.getFreeBytes(storageUUID)

        if (availableBytes < requiredBytes) {
            val allocatableBytes = storageManager.getAllocatableBytes(storageUUID)
            if (allocatableBytes + availableBytes >= requiredBytes) {
                val toAllocate = requiredBytes - availableBytes
                Timber.tag(TAG).v(
                    "Not enough free space, allocating %d on %s.", requiredBytes, targetPath
                )
                storageManager.allocateBytes(storageUUID, toAllocate)
                availableBytes += toAllocate
            }
        }

        return CheckResult(
            path = targetPath,
            isSpaceAvailable = availableBytes >= requiredBytes || requiredBytes == -1L,
            requiredBytes = requiredBytes,
            freeBytes = availableBytes,
            totalBytes = totalBytes
        )
    }

    private fun requestStorageLegacy(targetPath: File, requiredBytes: Long = -1L): CheckResult {
        Timber.tag(TAG).v(
            "requestStorageAPI26Plus(path=%s, requiredBytes=%d)", targetPath, requiredBytes
        )

        val stats = statsFsProvider.createStats(targetPath)

        return CheckResult(
            path = targetPath,
            isSpaceAvailable = stats.availableBytes >= requiredBytes || requiredBytes == -1L,
            requiredBytes = requiredBytes,
            freeBytes = stats.availableBytes,
            totalBytes = stats.totalBytes
        )
    }

    private suspend fun checkSpace(
        path: File,
        requiredBytes: Long = -1L
    ): CheckResult = withContext(Dispatchers.IO) {
        try {
            Timber.tag(TAG).v("checkSpace(path=%s, requiredBytes=%d)", path, requiredBytes)
            val result: CheckResult = if (apiLevel.hasAPILevel(Build.VERSION_CODES.O)) {
                try {
                    requestStorageAPI26Plus(path, requiredBytes)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "requestStorageAPI26Plus() failed")
                    requestStorageLegacy(path, requiredBytes)
                }
            } else {
                requestStorageLegacy(path, requiredBytes)
            }

            Timber.tag(TAG).d("Requested %d from %s: %s", requiredBytes, path, result)
            return@withContext result
        } catch (e: Exception) {
            throw IOException("checkSpace(path=$path, requiredBytes=$requiredBytes) FAILED", e)
                .also { Timber.tag(TAG).e(it) }
        }
    }

    /**
     * Returns an **[CheckResult]** telling you how much private storage is available to us
     * Pass **[requiredBytes]** if we should attempt to free space if not enough is available.
     * This may cause the system to delete caches to free space.
     *
     * Don't call this on the UI thread as the operation may block due to IO.
     *
     * @throws IOException if storage check or allocation fails.
     */
    suspend fun checkSpacePrivateStorage(requiredBytes: Long = -1L): CheckResult =
        checkSpace(privateStorage, requiredBytes)

    /**
     * Like **[checkSpacePrivateStorage]** but throws **[InsufficientStorageException]**
     * if not enough is available
     */
    suspend fun requireSpacePrivateStorage(requiredBytes: Long = -1L): CheckResult =
        checkSpace(privateStorage, requiredBytes).apply {
            if (!isSpaceAvailable) throw InsufficientStorageException(this)
        }

    data class CheckResult(
        val path: File,
        val isSpaceAvailable: Boolean,
        val requiredBytes: Long = -1L,
        val freeBytes: Long,
        val totalBytes: Long
    ) {

        fun getFormattedFreeSpace(context: Context): String =
            Formatter.formatShortFileSize(context, freeBytes)

        fun getFormattedTotalSpace(context: Context): String =
            Formatter.formatShortFileSize(context, totalBytes)
    }

    companion object {
        val TAG = DeviceStorage::class.java.simpleName
    }
}
