package de.rki.coronawarnapp.profile.ui.qrcode

import dagger.Reusable
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.util.TimeStamper
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Reusable
class VCard @Inject constructor(
    private val timeStamper: TimeStamper
) {

    private val now = timeStamper.nowJavaUTC

    /**
     * Return V-Card format for [RATProfile]
     * @return [String]
     */
    fun create(profile: Profile): String = profile.run {
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
        val birthDate = birthDate?.let { birthDateFormatter.format(it) }.orEmpty()
        val rev = revDateFormatter.format(now) // Time the vCard was updated
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

    companion object {
        private val birthDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        private val revDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ")
    }
}
