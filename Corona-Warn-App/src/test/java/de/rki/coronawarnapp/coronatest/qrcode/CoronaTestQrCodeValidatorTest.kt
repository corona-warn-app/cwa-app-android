package de.rki.coronawarnapp.coronatest.qrcode

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestQrCodeValidatorTest : BaseTest() {

    private val pcrQrCode1 = "HTTPS://LOCALHOST/?123456-12345678-1234-4DA7-B166-B86D85475064"
    private val pcrQrCode2 = "https://localhost/?123456-12345678-1234-4DA7-B166-B86D85475064"
    private val pcrQrCode3 = "https://LOCALHOST/?123456-12345678-1234-4DA7-B166-B86D85475064"
    private val raQrCode1 =
        "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2MTc3MDEzOTYsImd1aWQiOiJGMUQ2QTAtRjFENkEwQzYtNUM3RC00MjVFLTlCNEMtODQ2QTQ5MTVERkVFIn0"
    private val raQrCode2 =
        "https://s.coronawarn.app?v=1#eyJ0aW1lc3RhbXAiOjE2MTc3MDEzOTYsImd1aWQiOiJGMUQ2QTAtRjFENkEwQzYtNUM3RC00MjVFLTlCNEMtODQ2QTQ5MTVERkVFIn0"
    private val raQrCode3 =
        "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2MTc3MDEzOTMsImd1aWQiOiIxOEUxRTktMThFMUU5OUUtRjk0OS00MDkzLUEwMjgtQUU0QjJEOTI5QTRDIiwiZm4iOiJMaWxsaWUiLCJsbiI6IkNhbXBiZWxsIiwiZG9iIjoiMTk1Ni0wNy0yMSJ9"

    @Test
    fun `valid codes are extracted by corresponding extractor`() {
        val instance = CoronaTestQrCodeValidator()
        instance.validate(pcrQrCode1).type shouldBe CoronaTest.Type.PCR
        instance.validate(pcrQrCode2).type shouldBe CoronaTest.Type.PCR
        instance.validate(pcrQrCode3).type shouldBe CoronaTest.Type.PCR
        instance.validate(raQrCode1).type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        instance.validate(raQrCode2).type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        instance.validate(raQrCode3).type shouldBe CoronaTest.Type.RAPID_ANTIGEN
    }
}
