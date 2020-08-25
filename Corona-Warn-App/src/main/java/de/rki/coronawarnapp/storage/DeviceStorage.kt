package de.rki.coronawarnapp.storage

import android.annotation.TargetApi
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.text.format.Formatter
import de.rki.coronawarnapp.util.ApiLevel
import de.rki.coronawarnapp.util.storage.StatsFsProvider
import timber.log.Timber
import java.io.File
import java.util.UUID

// TODO Add inject when #1069 is merged
class DeviceStorage constructor(
    private val context: Context,
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
            freeBytes = stats.availableBytes,
            totalBytes = stats.totalBytes
        )
    }

    private fun checkSpace(path: File, requiredBytes: Long = -1L): CheckResult =
        try {
            val result = if (apiLevel.hasAPILevel(Build.VERSION_CODES.O)) {
                requestStorageAPI26Plus(path, requiredBytes)
            } else {
                requestStorageLegacy(path, requiredBytes)
            }
            Timber.tag(TAG).d("Requested %d from %s: %s", requiredBytes, path, result)
            result
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to request %d on %s", requiredBytes, path)
            // Android storage is a fickle beast
            // If the space request fails, it's possible that the write attempt will still succeed
            CheckResult(path, true, -1L, -1L)
        }

    /**
     * Returns an **[CheckResult]** telling you how much private storage is available to us
     * Pass **[requiredBytes]** if we should attempt to free space if not enough is available.
     * This may cause the system to delete caches to free space.
     * If there are any errors you'll get a result with **[CheckResult.isSpaceAvailable]**,
     * but **[CheckResult.freeBytes]** == -1L
     */
    fun checkSpacePrivateStorage(requiredBytes: Long = -1L): CheckResult =
        checkSpace(privateStorage, requiredBytes)

    data class CheckResult(
        val path: File,
        val isSpaceAvailable: Boolean,
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
