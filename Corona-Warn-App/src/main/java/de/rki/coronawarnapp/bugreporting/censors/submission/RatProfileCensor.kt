package de.rki.coronawarnapp.bugreporting.censors.submission

import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidAddress
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidCity
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidPhoneNumber
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidZipCode
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import kotlinx.coroutines.flow.first
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class RatProfileCensor @Inject constructor(
    private val ratProfileSettings: RATProfileSettings
) : BugCensor {

    private val dayOfBirthFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    override suspend fun checkLog(entry: LogLine): LogLine? {
        val ratProfile = ratProfileSettings.profile.flow.first() ?: return null

        var newMessage = entry.message

        with(ratProfile) {
            withValidName(firstName) { firstName ->
                newMessage = newMessage.replace(firstName, "RAT-Profile/FirstName")
            }

            withValidName(lastName) { lastName ->
                newMessage = newMessage.replace(lastName, "RAT-Profile/LastName")
            }

            val dateOfBirthString = birthDate?.toString(dayOfBirthFormatter)

            if (dateOfBirthString != null) {
                newMessage = newMessage.replace(dateOfBirthString, "RAT-Profile/DateOfBirth")
            }

            withValidAddress(street) { street ->
                newMessage = newMessage.replace(street, "RAT-Profile/Street")
            }

            withValidCity(city) { city ->
                newMessage = newMessage.replace(city, "RAT-Profile/City")
            }

            withValidZipCode(zipCode) { zipCode ->
                newMessage = newMessage.replace(zipCode, "RAT-Profile/Zip-Code")
            }

            withValidPhoneNumber(phone) { phone ->
                newMessage = newMessage.replace(phone, "RAT-Profile/Phone")
            }

            withValidPhoneNumber(email) { phone ->
                newMessage = newMessage.replace(phone, "RAT-Profile/eMail")
            }
        }

        return entry.toNewLogLineIfDifferent(newMessage)
    }
}
