package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.CoronaWarnApplication
import timber.log.Timber
import java.io.File
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
     *
     * TODO Check with UX team to handle insufficient space flow
     */
    fun checkFileStorageFreeSpace() {
        val availableSpace = (keyExportDirectory.usableSpace / BYTES)
            .also { logAvailableSpace(it) }

        if (availableSpace < FileStorageConstants.FREE_SPACE_THRESHOLD) {
            logInsufficientSpace(availableSpace)
        }
    }

    /**
     * Remove outdated key files from internal storage.
     * Threshold: FileStorageConstants.DAYS_TO_KEEP
     */
    fun removeOutdatedFilesFromStorage() {
        keyExportDirectory
            .walk()
            .filter(File::isDirectory)
            .forEach { file: File ->
                Unit
                if (file != keyExportDirectory && file.isOutdated()) {
                    file.checkAndRemove().also { logFileRemovalResult(file.name, it) }
                }
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
