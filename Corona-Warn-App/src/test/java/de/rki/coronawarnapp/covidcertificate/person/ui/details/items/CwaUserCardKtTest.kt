package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import de.rki.coronawarnapp.covidcertificate.common.certificate.parseLocalDate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class CwaUserCardKtTest : BaseTest() {

    @Test
    fun formatBirthDateTest() {
        formatBirthDate("1980-10-20") shouldBe
            "1980-10-20".parseLocalDate()!!.toShortDayFormat()

        formatBirthDate("1980") shouldBe "1980"
        formatBirthDate("1980-10") shouldBe "1980-10"
    }
}
