package de.rki.coronawarnapp.coronatest.qrcode

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RapidAntigenQrCodeExtractorTest : BaseTest() {

    private val qrCode1 =
        "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2MTc3MDEzOTYsImd1aWQiOiJGMUQ2QTAtRjFENkEwQzYtNUM3RC00MjVFLTlCNEMtODQ2QTQ5MTVERkVFIn0"
    private val qrCode2 =
        "https://s.coronawarn.app?v=1#eyJ0aW1lc3RhbXAiOjE2MTc3MDEzOTYsImd1aWQiOiJGMUQ2QTAtRjFENkEwQzYtNUM3RC00MjVFLTlCNEMtODQ2QTQ5MTVERkVFIn0"
    private val qrCode3 =
        "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2MTc3MDEzOTMsImd1aWQiOiIxOEUxRTktMThFMUU5OUUtRjk0OS00MDkzLUEwMjgtQUU0QjJEOTI5QTRDIiwiZm4iOiJMaWxsaWUiLCJsbiI6IkNhbXBiZWxsIiwiZG9iIjoiMTk1Ni0wNy0yMSJ9"
    private val qrCode4 =
        "https://s.coronawarn.app?v=1#eyJ0aW1lc3RhbXAiOjE2MTc3MDEzOTMsImd1aWQiOiIxOEUxRTktMThFMUU5OUUtRjk0OS00MDkzLUEwMjgtQUU0QjJEOTI5QTRDIiwiZm4iOiJMaWxsaWUiLCJsbiI6IkNhbXBiZWxsIiwiZG9iIjoiMTk1Ni0wNy0yMSJ9"
    private val qrCode5 =
        "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2MTgyMTEzNjQsImd1aWQiOiI0NTQyOTMtNDU0MjkzMzgtN0M0QS00QkY5LTg3ODYtMjIxMDg1RjQ5RURCIn0="
    private val qrCode6 =
        "https://s.coronawarn.app?v=1#eyJ0aW1lc3RhbXAiOjE2MTgyMTEzNjQsImd1aWQiOiI0NTQyOTMtNDU0MjkzMzgtN0M0QS00QkY5LTg3ODYtMjIxMDg1RjQ5RURCIn0="
    private val qrCode7 =
        "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2MTgyMTEwMjksImd1aWQiOiIzNDlDNDUtMzQ5QzQ1MjItNzFDOC00MDlDLUJFRTgtMEZFMTA2MDU5MEY2IiwiZm4iOiJ+ZGEiLCJsbiI6IkhvcG1hbiIsImRvYiI6IjIwMDAtMDUtMjUifQ=="
    private val qrCode8 =
        "https://s.coronawarn.app?v=1#eyJ0aW1lc3RhbXAiOjE2MTgyMTEwMjksImd1aWQiOiIzNDlDNDUtMzQ5QzQ1MjItNzFDOC00MDlDLUJFRTgtMEZFMTA2MDU5MEY2IiwiZm4iOiJ+ZGEiLCJsbiI6IkhvcG1hbiIsImRvYiI6IjIwMDAtMDUtMjUifQ=="

    private val pcrQrCode1 = "HTTPS://LOCALHOST/?123456-12345678-1234-4DA7-B166-B86D85475064"
    private val pcrQrCode2 = "https://localhost/?123456-12345678-1234-4DA7-B166-B86D85475064"
    private val pcrQrCode3 = "https://LOCALHOST/?123456-12345678-1234-4DA7-B166-B86D85475064"

    private val instance = RapidAntigenQrCodeExtractor()

    @Test
    fun `valid codes are recognized`() {
        listOf(qrCode1, qrCode2, qrCode3, qrCode4, qrCode5, qrCode6, qrCode7, qrCode8).forEach {
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
        listOf(qrCode1, qrCode2, qrCode3, qrCode4, qrCode5, qrCode6, qrCode7, qrCode8).forEach {
            instance.extract(it)
        }
    }

    @Test
    fun `personal data is extracted`() {
        val data = instance.extract(qrCode3)
        data.type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        data.guid shouldBe "18E1E9-18E1E99E-F949-4093-A028-AE4B2D929A4C"
        data.createdAt shouldBe Instant.ofEpochMilli(1617701393000)
        data.dateOfBirth shouldBe LocalDate.parse("1956-07-21")
        data.lastName shouldBe "Campbell"
        data.firstName shouldBe "Lillie"
    }
}
