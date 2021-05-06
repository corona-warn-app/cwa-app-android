package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.util.CWADebug
import javax.inject.Inject

@Reusable
class PcrQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(entry: LogLine): LogLine? {

        val guid = lastGUID ?: return null
        if (!entry.message.contains(guid)) return null

        val newMessage = if (CWADebug.isDeviceForTestersBuild) {
            entry.message.replace(guid, PLACEHOLDER_TESTER + guid.takeLast(27))
        } else {
            entry.message.replace(guid, PLACEHOLDER + guid.takeLast(4))
        }

        return entry.toNewLogLineIfDifferent(newMessage)
    }

    companion object {
        var lastGUID: String? = null
        private const val PLACEHOLDER_TESTER = "########-"
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
