package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import de.rki.coronawarnapp.covidcertificate.common.certificate.parseLocalDate
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal class CwaUserCardKtTest : BaseTest() {

    @Test
    fun formatBirthDateTest() {
        formatBirthDate("1980-10-20") shouldBe
            "1980-10-20".parseLocalDate()!!.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

        formatBirthDate("1980") shouldBe "1980"
        formatBirthDate("1980-10") shouldBe "1980-10"
    }
}
