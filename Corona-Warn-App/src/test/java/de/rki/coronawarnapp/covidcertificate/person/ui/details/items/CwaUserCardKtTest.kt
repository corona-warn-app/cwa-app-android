package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import io.kotest.matchers.shouldBe
import org.joda.time.DateTimeZone

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import java.util.Locale
import java.util.TimeZone

internal class CwaUserCardKtTest : BaseTest() {

    @BeforeEach
    fun setup() {
        Locale.setDefault(Locale.GERMAN)

        val timeZone = TimeZone.getTimeZone("Europe/Berlin")
        TimeZone.setDefault(timeZone)
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(timeZone))
    }

    @Test
    fun formatBirthDateTest() {
        formatBirthDate("1980-10-20") shouldBe "20.10.80"
        formatBirthDate("1980") shouldBe "1980"
        formatBirthDate("1980-10") shouldBe "1980-10"
    }
}
