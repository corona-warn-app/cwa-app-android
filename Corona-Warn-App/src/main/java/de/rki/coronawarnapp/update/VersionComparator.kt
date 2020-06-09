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
        
        // ** please check my attached comments to the following code block =>

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

/*
** Dear Team, 

I just wish to notify you that I think to have found a (logical) bug in your above code. 
In my opinion the following code snippet below will fail, under the following constructed example condition:

        if (versionToCompareMajor > currentVersionMajor) {
            isVersionOlder = true
        } else if (versionToCompareMinor > currentVersionMinor) {
            isVersionOlder = true

currentMajor version = 3, compareMajor version = 1
currentMinor version = 1, compareMinor version = 2

In other words: If the major version on the server is two major steps older than the installed one, 
the function "isVersionOlder" will the faulty return true, which in turn would update the App to an actually *older* version. 
Thus the app will effectively be downgraded instead of upgraded, so to say.
If this behaviour is intended, then please skip my remarks. 
(It just appeared slightly odd to me, alone from the viewpoint of the logical construction of the code.)
Hope this was a bit helpful in any case.

Kind Regards
Dan(iel)
*/
