package de.rki.coronawarnapp.bugreporting.censors

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.CWADebug
import javax.inject.Inject
import kotlin.math.min

@Reusable
class RegistrationTokenCensor @Inject constructor() : BugCensor {
    override fun checkLog(entry: LogLine): LogLine? {
        val token = LocalData.registrationToken() ?: return null
        if (!entry.message.contains(token)) return null

        val replacement = if (CWADebug.isDeviceForTestersBuild) {
            token.substring(0, min(4, token.length)) + "###-####-####-####-############"
        } else {
            token
        }
        return entry.copy(message = entry.message.replace(token, replacement))
    }
}
