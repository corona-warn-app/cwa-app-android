package de.rki.coronawarnapp.coronatest.qrcode

import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor.Mode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestQrCodeValidatorTest : BaseTest() {
    private val raExtractor = spyk(RapidAntigenQrCodeExtractor())
    private val pcrExtractor = spyk(PcrQrCodeExtractor())

    @Test
    fun `valid codes are extracted by corresponding extractor`() {
        val instance = CoronaTestQrCodeValidator(raExtractor, pcrExtractor)
        instance.validate(pcrQrCode1).type shouldBe CoronaTest.Type.PCR
        instance.validate(pcrQrCode2).type shouldBe CoronaTest.Type.PCR
        instance.validate(pcrQrCode3).type shouldBe CoronaTest.Type.PCR
        instance.validate(raQrCode1).type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        instance.validate(raQrCode2).type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        instance.validate(raQrCode3).type shouldBe CoronaTest.Type.RAPID_ANTIGEN
    }

    @Test
    fun `invalid prefix throws exception`() {
        val invalidCode = "HTTPS://somethingelse/?123456-12345678-1234-4DA7-B166-B86D85475064"
        val instance = CoronaTestQrCodeValidator(raExtractor, pcrExtractor)
        shouldThrow<InvalidQRCodeException> {
            instance.validate(invalidCode)
        }
    }

    @Test
    fun `invalid json throws exception`() {
        val invalidCode = "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2"
        val instance = CoronaTestQrCodeValidator(raExtractor, pcrExtractor)
        shouldThrow<InvalidQRCodeException> {
            instance.validate(invalidCode)
        }
    }

    @Test
    fun `validator uses strict extraction mode`() {
        val instance = CoronaTestQrCodeValidator(raExtractor, pcrExtractor)
        instance.validate(pcrQrCode1).type shouldBe CoronaTest.Type.PCR
        verify { pcrExtractor.extract(pcrQrCode1, Mode.TEST_STRICT) }
        instance.validate(raQrCode1).type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        verify { raExtractor.extract(raQrCode1, Mode.TEST_STRICT) }
    }
}
