package de.rki.coronawarnapp.update

/**
 * Helper to compare 2 version strings
 */
object VersionComparator {

    /**
     * Checks if currentVersion is older than versionToCompareTo
     *
     * Expected input format: <major>.<minor>.<patch>
     * major, minor and patch are Int
     *
     * @param currentVersion
     * @param versionToCompareTo
     * @return true if currentVersion is older than versionToCompareTo, else false
     */
    fun isVersionOlder(currentVersion: Long, versionToCompareTo: Long): Boolean {
        return currentVersion < versionToCompareTo
    }
}
