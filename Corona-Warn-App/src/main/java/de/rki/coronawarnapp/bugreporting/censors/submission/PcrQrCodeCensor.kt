package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import javax.inject.Inject

@Reusable
class PcrQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): CensorContainer? {
        val guid = lastGUID ?: return null
        if (!message.contains(guid)) return null

        return CensorContainer(message).censor(guid, PLACEHOLDER + guid.takeLast(4)).nullIfEmpty()
    }

    companion object {
        var lastGUID: String? = null
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
