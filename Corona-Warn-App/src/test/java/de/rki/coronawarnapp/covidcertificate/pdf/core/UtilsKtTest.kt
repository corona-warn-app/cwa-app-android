package de.rki.coronawarnapp.covidcertificate.pdf.core

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import java.util.Locale

internal class UtilsKtTest : BaseTest() {

    @Test
    fun `Test issuerCountryDisplayName`() {
        issuerCountryDisplayName("DE", Locale.GERMAN) shouldBe "Deutschland"
        issuerCountryDisplayName("DE", Locale.ENGLISH) shouldBe "Germany"
        issuerCountryDisplayName("DE", Locale.ITALIAN) shouldBe "Germany"
        issuerCountryDisplayName("DE", Locale.JAPANESE) shouldBe "Germany"
    }
}
