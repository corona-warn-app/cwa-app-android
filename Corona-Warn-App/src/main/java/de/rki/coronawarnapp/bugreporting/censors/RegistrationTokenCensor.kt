package de.rki.coronawarnapp.bugreporting.censors

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.tryNewMessage
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.CWADebug
import javax.inject.Inject

@Reusable
class RegistrationTokenCensor @Inject constructor() : BugCensor {
    override suspend fun checkLog(entry: LogLine): LogLine? {
        val token = LocalData.registrationToken() ?: return null
        if (!entry.message.contains(token)) return null

        val newMessage = if (CWADebug.isDeviceForTestersBuild) {
            entry.message.replace(token, PLACEHOLDER_TESTER + token.takeLast(27))
        } else {
            entry.message.replace(token, PLACEHOLDER + token.takeLast(4))
        }

        return entry.tryNewMessage(newMessage)
    }

    companion object {
        private const val PLACEHOLDER_TESTER = "########-"
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
