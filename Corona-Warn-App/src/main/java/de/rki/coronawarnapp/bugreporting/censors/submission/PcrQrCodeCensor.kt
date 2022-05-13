package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import java.time.LocalDate
import javax.inject.Inject

@Reusable
class PcrQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): CensorContainer? {
        var container = CensorContainer(message)
        lastGUID?.let {
            container = container.censor(it, PLACEHOLDER + it.takeLast(4))
        }
        dateOfBirth?.toString()?.let {
            container = container.censor(it, "PcrTest/DateOfBirth")
        }
        return container.nullIfEmpty()
    }

    companion object {
        var lastGUID: String? = null
        var dateOfBirth: LocalDate? = null
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
