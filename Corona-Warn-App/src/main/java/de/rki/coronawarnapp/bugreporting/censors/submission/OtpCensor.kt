package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import kotlinx.coroutines.flow.last
import javax.inject.Inject

@Reusable
class OtpCensor @Inject constructor(
    private val srsSubmissionSettings: SrsSubmissionSettings,
) : BugCensor {

    override suspend fun checkLog(message: String): BugCensor.CensorContainer? {
        var container = BugCensor.CensorContainer(message)

        val otp = srsSubmissionSettings.otp.last()

        otp?.uuid?.let {
            container = container.censor(it.toString(), "########-####-####-####-########")
        }

        otp?.expiresAt?.toString()?.let {
            container = container.censor(it, "SrsOtp/expiresAt")
        }

        return container.nullIfEmpty()
    }
}
