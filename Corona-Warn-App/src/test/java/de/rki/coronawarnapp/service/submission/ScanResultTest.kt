package de.rki.coronawarnapp.service.submission

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test

class ScanResultTest {
    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val lowerCaseGuid = "123456-12345678-1234-4da7-b166-b86d85475064"
    private val mixedCaseGuid = "123456-12345678-1234-4dA7-b166-B86d85475064"

    @MockK
    private lateinit var scanResult: QRScanResult

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(scanResult)
        every { scanResult.isValid } returns false
    }

    @Test
    fun containsValidGUID() {
        // valid test
        scanResult = QRScanResult("https://localhost/?$guid")
        scanResult.isValid shouldBe true

        //UPPERCASE and MIXED Cases Strings
        scanResult = QRScanResult("https://localhost/?$lowerCaseGuid")
        scanResult.isValid shouldBe true

        scanResult = QRScanResult("HTTPS://LOCALHOST/?$lowerCaseGuid")
        scanResult.isValid shouldBe true

        scanResult = QRScanResult("https://localhost/?$mixedCaseGuid")
        scanResult.isValid shouldBe true

        scanResult = QRScanResult("https://LOCALHOST/?$mixedCaseGuid")
        scanResult.isValid shouldBe true

        //extra slashes should be invalid.
        scanResult = QRScanResult("HTTPS:///LOCALHOST/?$guid")
        scanResult.isValid shouldBe false

        scanResult = QRScanResult("HTTPS://LOCALHOST//?$guid")
        scanResult.isValid shouldBe false

        scanResult = QRScanResult("HTTPS://LOCALHOST///?$guid")
        scanResult.isValid shouldBe false

        // more invalid tests checks
        scanResult = QRScanResult("http://localhost/?$guid")
        scanResult.isValid shouldBe false
        scanResult = QRScanResult("https://localhost/?")
        scanResult.isValid shouldBe false
        scanResult = QRScanResult("htps://wrongformat.com")
        scanResult.isValid shouldBe false
        scanResult =
            QRScanResult("https://localhost/%20?3D6D08-3567F3F2-4DCF-43A3-8737-4CD1F87D6FDA")
        scanResult.isValid shouldBe false
        scanResult =
            QRScanResult("https://some-host.com/?3D6D08-3567F3F2-4DCF-43A3-8737-4CD1F87D6FDA")
        scanResult.isValid shouldBe false
        scanResult = QRScanResult("https://localhost/?3567F3F2-4DCF-43A3-8737-4CD1F87D6FDA")
        scanResult.isValid shouldBe false
        scanResult = QRScanResult("https://localhost/?4CD1F87D6FDA")
        scanResult.isValid shouldBe false
    }

    @Test
    fun extractGUID() {
        QRScanResult("https://localhost/?$guid").guid shouldBe guid
        QRScanResult("https://LOCALHOST/?$lowerCaseGuid").guid shouldBe lowerCaseGuid
        QRScanResult("https://LOCALHOST/?$mixedCaseGuid").guid shouldBe mixedCaseGuid
    }
}
