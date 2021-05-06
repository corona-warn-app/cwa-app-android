package de.rki.coronawarnapp.coronatest.antigen.profile

import dagger.Reusable
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.format.ISODateTimeFormat
import javax.inject.Inject

@Reusable
class VCard @Inject constructor(
    private val timeStamper: TimeStamper
) {

    /**
     * Return V-Card format for [RATProfile]
     * @return [String]
     */
    fun create(ratProfile: RATProfile): String = ratProfile.run {
        val lastName = lastName.escapeAll()
        val firstName = firstName.escapeAll()
        val fullName = buildString {
            append(firstName)
            if (lastName.isNotBlank()) {
                append(" $lastName")
            }
        }
        val city = city.escapeAll()
        val street = street.escapeAll()
        val zipCode = zipCode.escapeAll()
        val phone = phone.escapeAll()
        val email = email.escapeAll()
        val birthDate = birthDate?.toString(ISODateTimeFormat.basicDate()).orEmpty()
        val rev = timeStamper.nowUTC.toString(ISODateTimeFormat.basicDateTimeNoMillis()) // Time the vCard was updated
        """
            BEGIN:VCARD
            VERSION:4.0
            N:$lastName;$firstName;;;
            FN:$fullName
            BDAY:$birthDate
            EMAIL;TYPE=home:$email
            TEL;TYPE="cell,home":$phone
            ADR;TYPE=home:;;$street;$city;;$zipCode
            REV:$rev
            END:VCARD
        """.trimIndent()
    }

    private fun String.escapeAll(): String = replace("\n", "")
        .replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
}
