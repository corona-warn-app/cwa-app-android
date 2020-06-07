package de.rki.coronawarnapp.update

object VersionComparator {

    fun isVersionOlder(currentVersion: String, versionToCompareTo: String): Boolean {
        var isVersionOlder = false

        val delimiter = "."

        val currentVersionParts = currentVersion.split(delimiter)
        val currentVersionMajor = currentVersionParts[0].toInt()
        val currentVersionMinor = currentVersionParts[1].toInt()
        val currentVersionPatch = currentVersionParts[2].toInt()

        val versionToCompareParts = versionToCompareTo.split(delimiter)
        val versionToCompareMajor = versionToCompareParts[0].toInt()
        val versionToCompareMinor = versionToCompareParts[1].toInt()
        val versionToComparePatch = versionToCompareParts[2].toInt()

        if (versionToCompareMajor > currentVersionMajor) {
            isVersionOlder = true
        } else if (versionToCompareMinor > currentVersionMinor) {
            isVersionOlder = true
        } else if (versionToComparePatch > currentVersionPatch) {
            isVersionOlder = true
        }

        return isVersionOlder
    }
}
