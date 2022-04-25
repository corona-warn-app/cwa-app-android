package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import de.rki.coronawarnapp.profile.legacy.RATProfile
import de.rki.coronawarnapp.profile.ui.create.RATProfileData
import de.rki.coronawarnapp.profile.ui.create.toRATProfile
import io.kotest.matchers.shouldBe
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RATProfileDataKtTest : BaseTest() {
    private val formatter = DateTimeFormat.forPattern("dd.MM.yyyy")

    private val ratProfileData = RATProfileData(
        firstName = "First name",
        lastName = "Last name",
        birthDate = null,
        street = "Main street",
        zipCode = "12132",
        city = "London",
        phone = "111111111",
        email = "email@example.com"
    )

    @Test
    fun `Map ratProfileData - birthDate is missing `() {
        ratProfileData.toRATProfile() shouldBe RATProfile(
            firstName = "First name",
            lastName = "Last name",
            birthDate = null,
            street = "Main street",
            zipCode = "12132",
            city = "London",
            phone = "111111111",
            email = "email@example.com"
        )
    }

    @Test
    fun `Map ratProfileData - birthDate exists`() {
        val birthDate = formatter.parseDateTime("01.01.1980").toLocalDate()
        ratProfileData.copy(birthDate = birthDate).toRATProfile() shouldBe
            RATProfile(
                firstName = "First name",
                lastName = "Last name",
                birthDate = birthDate,
                street = "Main street",
                zipCode = "12132",
                city = "London",
                phone = "111111111",
                email = "email@example.com"
            )
    }
}
