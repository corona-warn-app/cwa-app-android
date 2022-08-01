package de.rki.coronawarnapp.bugreporting.censors.dcc

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Reusable
class CwaUserCensor @Inject constructor(
    private val personCertificatesSettings: PersonCertificatesSettings
) : BugCensor {

    override suspend fun checkLog(message: String): CensorContainer? {
        var newMessage = CensorContainer(message)

        personCertificatesSettings.currentCwaUser.first()?.let { cwaUser ->
            newMessage = censorCwaUserData(cwaUser, newMessage)
        }

        return newMessage.nullIfEmpty()
    }

    private fun censorCwaUserData(
        cwaUser: CertificatePersonIdentifier,
        message: CensorContainer
    ): CensorContainer {
        var newMessage = message

        newMessage = newMessage.censor(
            cwaUser.dateOfBirthFormatted,
            "cwaUser/dateOfBirth"
        )

        newMessage = newMessage.censor(
            cwaUser.lastNameStandardized.orEmpty(),
            "cwaUser/lastNameStandardized"
        )

        cwaUser.firstNameStandardized?.let { firstNameStandardized ->
            newMessage = newMessage.censor(
                firstNameStandardized,
                "cwaUser/firstNameStandardized"
            )
        }

        return newMessage
    }
}
