package de.rki.coronawarnapp.storage

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.NotEnoughSpaceOnDiskException
import timber.log.Timber
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * A helper class for file storage manipulation
 * The helper uses externalised constants for readability.
 *
 * @see FileStorageConstants
 */
object FileStorageHelper {

    private val TAG: String? = FileStorageHelper::class.simpleName
    private val TIME_TO_KEEP = TimeUnit.DAYS.toMillis(FileStorageConstants.DAYS_TO_KEEP)
    private const val BYTES = 1048576

    /**
     * create the needed key export directory (recursively)
     *
     */
    fun initializeExportSubDirectory() = keyExportDirectory.mkdirs()

    /**
     * Get key files export directory used to store all export files for the transaction
     * Uses FileStorageConstants.KEY_EXPORT_DIRECTORY_NAME constant
     *
     * @return File of key export directory
     */
    val keyExportDirectory = File(
        CoronaWarnApplication.getAppContext().cacheDir,
        FileStorageConstants.KEY_EXPORT_DIRECTORY_NAME
    )

    /**
     * Checks if internal store has free memory.
     * Threshold: FileStorageConstants.FREE_SPACE_THRESHOLD
     * Bound to .usableSpace due to API level restrictions (minimum required level - 23)
     */
    fun checkFileStorageFreeSpace() {
        val availableSpace = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val storageManager = CoronaWarnApplication.getAppContext()
                .getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolume = storageManager.primaryStorageVolume
            val storageUUID =
                UUID.fromString(storageVolume.uuid ?: StorageManager.UUID_DEFAULT.toString())
            storageManager.getAllocatableBytes(storageUUID) / BYTES
        } else {
            keyExportDirectory.usableSpace / BYTES
        }
        if (availableSpace < FileStorageConstants.FREE_SPACE_THRESHOLD) {
            throw NotEnoughSpaceOnDiskException()
        }
    }

    fun getAllFilesInKeyExportDirectory(): List<File> {
        return keyExportDirectory
            .walk(FileWalkDirection.BOTTOM_UP)
            .filter(File::isFile)
            .toList()
    }

    fun File.isOutdated(): Boolean =
        (System.currentTimeMillis() - lastModified() > TIME_TO_KEEP)

    private fun File.checkAndRemove(): Boolean {
        return if (exists() && isDirectory) {
            deleteRecursively()
        } else {
            false
        }
    }

    // LOGGING
    private fun logFileRemovalResult(fileName: String, result: Boolean) =
        Timber.d("File $fileName was deleted: $result")

    private fun logAvailableSpace(availableSpace: Long) =
        Timber.d("Available space: $availableSpace")

    private fun logInsufficientSpace(availableSpace: Long) =
        Timber.e("Not enough free space! Required: ${FileStorageConstants.FREE_SPACE_THRESHOLD} Has: $availableSpace")
}
