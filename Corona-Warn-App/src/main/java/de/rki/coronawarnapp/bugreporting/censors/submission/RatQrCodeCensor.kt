package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.qrcode.RapidAntigenHash
import de.rki.coronawarnapp.util.CWADebug
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

@Reusable
class RatQrCodeCensor @Inject constructor() : BugCensor {

    private val dayOfBirthFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    override suspend fun checkLog(entry: LogLine): LogLine? {

        val dataToCensor = dataToCensor ?: return null

        var newMessage = entry.message

        with(dataToCensor) {
            newMessage = newMessage.replace(rawString, "RatQrCode/ScannedRawString")

            newMessage = if (CWADebug.isDeviceForTestersBuild) {
                newMessage.replace(hash, PLACEHOLDER + hash.takeLast(28))
            } else {
                newMessage.replace(hash, PLACEHOLDER + hash.takeLast(4))
            }

            withValidName(firstName) { firstName ->
                newMessage = newMessage.replace(firstName, "RATest/FirstName")
            }

            withValidName(lastName) { lastName ->
                newMessage = newMessage.replace(lastName, "RATest/LastName")
            }

            val dateOfBirthString = dateOfBirth?.toString(dayOfBirthFormatter) ?: return@with

            newMessage = newMessage.replace(dateOfBirthString, "RATest/DateOfBirth")
        }

        return entry.toNewLogLineIfDifferent(newMessage)
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
