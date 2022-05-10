package de.rki.coronawarnapp.coronatest.qrcode

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test
import testhelpers.BaseTest

class PcrQrCodeExtractorTest : BaseTest() {
    private val guidUpperCase = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val guidLowerCase = "123456-12345678-1234-4da7-b166-b86d85475064"
    private val guidMixedCase = "123456-12345678-1234-4dA7-b166-B86d85475064"
    private val localhostUpperCase = "HTTPS://LOCALHOST/?"
    private val localhostLowerCase = "https://localhost/?"
    private val localhostMixedCase = "https://LOCALHOST/?"

    private fun buildQRCodeCases(prefixString: String, guid: String, conditionToMatch: Boolean) = runTest {
        val extractor = PcrQrCodeExtractor()
        try {
            if (extractor.canHandle("$prefixString$guid")) {
                extractor.extract("$prefixString$guid")
                conditionToMatch shouldBe true
            } else {
                conditionToMatch shouldBe false
            }
        } catch (e: InvalidQRCodeException) {
            conditionToMatch shouldBe false
        }
    }

    @Test
    fun containsValidGUID() {
        // valid test

        buildQRCodeCases(localhostUpperCase, guidUpperCase, true)
        buildQRCodeCases(localhostLowerCase, guidUpperCase, true)
        buildQRCodeCases(localhostMixedCase, guidUpperCase, true)

        buildQRCodeCases(localhostUpperCase, guidLowerCase, true)
        buildQRCodeCases(localhostLowerCase, guidLowerCase, true)
        buildQRCodeCases(localhostMixedCase, guidLowerCase, true)

        buildQRCodeCases(localhostUpperCase, guidMixedCase, true)
        buildQRCodeCases(localhostLowerCase, guidMixedCase, true)
        buildQRCodeCases(localhostMixedCase, guidMixedCase, true)
    }

    @Test
    fun containsInvalidGUID() {
        // extra slashes should be invalid.
        buildQRCodeCases("HTTPS:///LOCALHOST/?", guidUpperCase, false)
        buildQRCodeCases("HTTPS://LOCALHOST//?", guidUpperCase, false)
        buildQRCodeCases("HTTPS://LOCALHOST///?", guidUpperCase, false)

        // more invalid tests checks
        buildQRCodeCases("http://localhost/?", guidUpperCase, false)
        buildQRCodeCases("https://localhost/?", "", false)
        buildQRCodeCases(
            "https://localhost/%20?3D6D08-3567F3F2-4DCF-43A3-8737-4CD1F87D6FDA",
            "",
            false
        )
        buildQRCodeCases(
            "https://some-host.com/?3D6D08-3567F3F2-4DCF-43A3-8737-4CD1F87D6FDA",
            "",
            false
        )
        buildQRCodeCases(
            "https://localhost/?3567F3F2-4DCF-43A3-8737-4CD1F87D6FDA",
            "",
            false
        )
        buildQRCodeCases(
            "https://localhost/?4CD1F87D6FDA",
            "",
            false
        )
    }

    @Test
    fun extractGUID() = runTest {
        PcrQrCodeExtractor().extract(
            "$localhostUpperCase$guidUpperCase",
        ).qrCodeGUID shouldBe guidUpperCase
        PcrQrCodeExtractor().extract(
            "$localhostUpperCase$guidLowerCase",
        ).qrCodeGUID shouldBe guidLowerCase
        PcrQrCodeExtractor().extract(
            "$localhostUpperCase$guidMixedCase",
        ).qrCodeGUID shouldBe guidMixedCase

        PcrQrCodeExtractor().extract(
            "$localhostLowerCase$guidUpperCase",
        ).qrCodeGUID shouldBe guidUpperCase
        PcrQrCodeExtractor().extract(
            "$localhostLowerCase$guidLowerCase",
        ).qrCodeGUID shouldBe guidLowerCase
        PcrQrCodeExtractor().extract(
            "$localhostLowerCase$guidMixedCase",
        ).qrCodeGUID shouldBe guidMixedCase

        PcrQrCodeExtractor().extract(
            "$localhostMixedCase$guidUpperCase",
        ).qrCodeGUID shouldBe guidUpperCase
        PcrQrCodeExtractor().extract(
            "$localhostMixedCase$guidLowerCase",
        ).qrCodeGUID shouldBe guidLowerCase
        PcrQrCodeExtractor().extract(
            "$localhostMixedCase$guidMixedCase",
        ).qrCodeGUID shouldBe guidMixedCase
    }
}
