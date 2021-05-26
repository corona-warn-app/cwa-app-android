package de.rki.coronawarnapp.bugreporting.censors.submission

import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidAddress
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidCity
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidPhoneNumber
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidZipCode
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class RatProfileCensor @Inject constructor(
    private val ratProfileSettings: RATProfileSettings
) : BugCensor {

    private val mutex = Mutex()
    private val dayOfBirthFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    private val ratProfileHistory = mutableSetOf<RATProfile>()

    override suspend fun checkLog(message: String): BugCensor.CensoredString? = mutex.withLock {
        val ratProfile = ratProfileSettings.profile.flow.first()

        // store the profile in a property so we still have a reference after it was deleted by the user
        if (ratProfile != null) {
            ratProfileHistory.add(ratProfile)
        }

        var newMessage = CensorContainer.fromOriginal(message)

        ratProfileHistory.forEach { profile ->
            with(profile) {
                withValidName(firstName) { firstName ->
                    newMessage = newMessage.censor(firstName, "RAT-Profile/FirstName")
                }

                withValidName(lastName) { lastName ->
                    newMessage = newMessage.censor(lastName, "RAT-Profile/LastName")
                }

                val dateOfBirthString = birthDate?.toString(dayOfBirthFormatter)

                if (dateOfBirthString != null) {
                    newMessage = newMessage.censor(dateOfBirthString, "RAT-Profile/DateOfBirth")
                }

                withValidAddress(street) { street ->
                    newMessage = newMessage.censor(street, "RAT-Profile/Street")
                }

                withValidCity(city) { city ->
                    newMessage = newMessage.censor(city, "RAT-Profile/City")
                }

                withValidZipCode(zipCode) { zipCode ->
                    newMessage = newMessage.censor(zipCode, "RAT-Profile/Zip-Code")
                }

                withValidPhoneNumber(phone) { phone ->
                    newMessage = newMessage.censor(phone, "RAT-Profile/Phone")
                }

                withValidPhoneNumber(email) { phone ->
                    newMessage = newMessage.censor(phone, "RAT-Profile/eMail")
                }
            }
        }

        return newMessage.compile()
    }
}
