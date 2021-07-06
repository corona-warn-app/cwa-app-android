package de.rki.coronawarnapp.covidcertificate.validation.core.country

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Locale

class DccCountryTest : BaseTest() {

    @Test
    fun `human readable country name`() {
        DccCountry("de").displayName(Locale.GERMAN) shouldBe "Deutschland"
    }
}
