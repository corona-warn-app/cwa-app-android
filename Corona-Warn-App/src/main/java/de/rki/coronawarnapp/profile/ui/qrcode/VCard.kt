package de.rki.coronawarnapp.profile.ui.qrcode

import dagger.Reusable
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.util.TimeStamper
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Reusable
class VCard @Inject constructor(
    timeStamper: TimeStamper
) {

    private val now = timeStamper.nowUTC

    /**
     * Return V-Card format for [Profile]
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
        val birthDate = birthDate?.format(DateTimeFormatter.BASIC_ISO_DATE).orEmpty()
        val rev = revDateFormatter.format(now.atZone(ZoneOffset.UTC)) // Time the vCard was updated
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
        private val revDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
    }
}
