package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.coronatest.qrcode.RapidAntigenHash
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

@Reusable
class RatQrCodeCensor @Inject constructor() : BugCensor {

    private val dayOfBirthFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    override suspend fun checkLog(message: String): BugCensor.CensoredString? {

        val dataToCensor = dataToCensor ?: return null

        var newMessage = CensorContainer(message)

        with(dataToCensor) {
            newMessage = newMessage.censor(rawString, "RatQrCode/ScannedRawString")

            newMessage = newMessage.censor(hash, PLACEHOLDER + hash.takeLast(4))

            withValidName(firstName) { firstName ->
                newMessage = newMessage.censor(firstName, "RATest/FirstName")
            }

            withValidName(lastName) { lastName ->
                newMessage = newMessage.censor(lastName, "RATest/LastName")
            }

            val dateOfBirthString = dateOfBirth?.toString(dayOfBirthFormatter) ?: return@with

            newMessage = newMessage.censor(dateOfBirthString, "RATest/DateOfBirth")
        }

        return newMessage.compile()
    }

    companion object {
        var dataToCensor: CensorData? = null

        private const val PLACEHOLDER = "SHA256HASH-ENDING-WITH-"
    }

    data class CensorData(
        val rawString: String,
        val hash: RapidAntigenHash,
        val firstName: String?,
        val lastName: String?,
        val dateOfBirth: LocalDate?
    )
}
