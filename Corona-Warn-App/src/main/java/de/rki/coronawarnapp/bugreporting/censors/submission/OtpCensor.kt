package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import javax.inject.Inject

@Reusable
class OtpCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): BugCensor.CensorContainer? {
        var container = BugCensor.CensorContainer(message)

        otp?.uuid?.let {
            container = container.censor(it.toString(), "########-####-####-####-########")
        }

        otp?.expiresAt?.toString()?.let {
            container = container.censor(it, "SrsOtp/expiresAt")
        }

        return container.nullIfEmpty()
    }

    companion object {
        var otp: SrsOtp? = null
    }
}
