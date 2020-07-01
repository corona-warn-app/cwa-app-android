package de.rki.coronawarnapp.storage

/**
 * The File Storage constants are used inside the FileStorageHelper
 *
 * @see FileStorageHelper
 */
object FileStorageConstants {

    /** Days to keep data in internal storage */
    const val DAYS_TO_KEEP: Long = 14

    /** Size (Mb) threshold for free space check */
    const val FREE_SPACE_THRESHOLD = 15

    /** Key export directory name in internal storage */
    const val KEY_EXPORT_DIRECTORY_NAME = "key-export"
}
