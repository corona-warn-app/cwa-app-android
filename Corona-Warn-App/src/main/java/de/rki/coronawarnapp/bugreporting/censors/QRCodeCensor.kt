package de.rki.coronawarnapp.bugreporting.censors

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.util.CWADebug
import javax.inject.Inject

@Reusable
class QRCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(entry: LogLine): LogLine? {

        val guid = lastGUID ?: return null
        if (!entry.message.contains(guid)) return null

        var newMessage = entry.message.replace(guid, PLACEHOLDER + guid.takeLast(4))

        if (CWADebug.isDeviceForTestersBuild) {
            newMessage = entry.message
        }

        return entry.copy(message = newMessage)
    }

    companion object {
        var lastGUID: String? = null
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
