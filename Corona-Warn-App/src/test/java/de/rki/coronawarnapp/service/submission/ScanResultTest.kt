package de.rki.coronawarnapp.service.submission

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test

class ScanResultTest {
    private val guidUpperCase = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val guidLowerCase = "123456-12345678-1234-4da7-b166-b86d85475064"
    private val guidMixedCase = "123456-12345678-1234-4dA7-b166-B86d85475064"
    private val localhostUpperCase = "HTTPS://LOCALHOST/?"
    private val localhostLowerCase = "https://localhost/?"
    private val localhostMixedCase = "https://LOCALHOST/?"

    @MockK
    private lateinit var scanResult: QRScanResult

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(scanResult)
        every { scanResult.isValid } returns false
    }

    private fun buildQRCodeCases(prefixString: String, guid: String, conditionToMatch: Boolean) {
        scanResult = QRScanResult("$prefixString$guid")
        scanResult.isValid shouldBe conditionToMatch
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
    fun extractGUID() {
        QRScanResult("$localhostUpperCase$guidUpperCase").guid shouldBe guidUpperCase
        QRScanResult("$localhostUpperCase$guidLowerCase").guid shouldBe guidLowerCase
        QRScanResult("$localhostUpperCase$guidMixedCase").guid shouldBe guidMixedCase

        QRScanResult("$localhostLowerCase$guidUpperCase").guid shouldBe guidUpperCase
        QRScanResult("$localhostLowerCase$guidLowerCase").guid shouldBe guidLowerCase
        QRScanResult("$localhostLowerCase$guidMixedCase").guid shouldBe guidMixedCase

        QRScanResult("$localhostMixedCase$guidUpperCase").guid shouldBe guidUpperCase
        QRScanResult("$localhostMixedCase$guidLowerCase").guid shouldBe guidLowerCase
        QRScanResult("$localhostMixedCase$guidMixedCase").guid shouldBe guidMixedCase
    }
}
