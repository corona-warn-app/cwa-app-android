package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensoredString
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import javax.inject.Inject

@Reusable
class PcrQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): CensoredString? {
        val guid = lastGUID ?: return null
        if (!message.contains(guid)) return null

        return CensoredString(message).censor(guid, PLACEHOLDER + guid.takeLast(4))
    }

    companion object {
        var lastGUID: String? = null
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
