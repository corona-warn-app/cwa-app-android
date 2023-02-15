package de.rki.coronawarnapp.eol

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import java.time.ZonedDateTime

class AppEolTest : BaseTest() {
    @Test
    fun `Test isOel`() {
        isEol(ZonedDateTime.parse("2023-06-01T00:00:00+02:00")) shouldBe true
        isEol(ZonedDateTime.parse("2023-05-31T00:00:00+00:00")) shouldBe false
        isEol(ZonedDateTime.parse("2023-07-01T00:00:00+00:00")) shouldBe true
    }
}
