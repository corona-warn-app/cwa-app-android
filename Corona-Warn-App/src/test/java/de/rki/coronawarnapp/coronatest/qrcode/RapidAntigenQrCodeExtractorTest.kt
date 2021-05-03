package de.rki.coronawarnapp.coronatest.qrcode

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RapidAntigenQrCodeExtractorTest : BaseTest() {

    private val instance = RapidAntigenQrCodeExtractor()

    @Test
    fun `valid codes are recognized`() {
        listOf(
            raQrCode1,
            raQrCode2,
            raQrCode3,
            raQrCode4,
            raQrCode5,
            raQrCode6,
            raQrCode7,
            raQrCode8,
            raQrCode9withUmlaut
        ).forEach {
            instance.canHandle(it) shouldBe true
        }
    }

    @Test
    fun `invalid codes are rejected`() {
        listOf(pcrQrCode1, pcrQrCode2, pcrQrCode3).forEach {
            instance.canHandle(it) shouldBe false
        }
    }

    @Test
    fun `extracting valid codes does not throw exception`() {
        listOf(
            raQrCode1,
            raQrCode2,
            raQrCode3,
            raQrCode4,
            raQrCode5,
            raQrCode6,
            raQrCode7,
            raQrCode8,
            raQrCode9withUmlaut
        ).forEach {
            instance.extract(it)
        }
    }

    @Test
    fun `personal data is extracted`() {
        val data = instance.extract(raQrCode3)
        data.type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        data.hash shouldBe "7dce08db0d4abd5ac1d2498b571afb221ca947c75c847d05466b4cfe9d95dc66"
        data.createdAt shouldBe Instant.ofEpochMilli(1619618352000)
        data.dateOfBirth shouldBe LocalDate.parse("1963-03-17")
        data.lastName shouldBe "Tyler"
        data.firstName shouldBe "Jacob"
    }

    @Test
    fun `empty strings are treated as null or notset`() {
        val data = instance.extract(raQrCodeEmptyStrings)
        data.type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        data.hash shouldBe "d6e4d0181d8109bf05b346a0d2e0ef0cc472eed70d9df8c4b9ae5c7a009f3e34"
        data.createdAt shouldBe Instant.ofEpochMilli(1619012952000)
        data.dateOfBirth shouldBe null
        data.lastName shouldBe null
        data.firstName shouldBe null
    }

    @Test
    fun `personal data is only valid if complete or completely missing`() {
        shouldThrow<InvalidQRCodeException> { instance.extract(raQrIncompletePersonalData) }
    }

    @Test
    fun `invalid json throws exception`() {
        val invalidCode = "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2"
        shouldThrow<InvalidQRCodeException> {
            RapidAntigenQrCodeExtractor().extract(invalidCode)
        }
    }
}
