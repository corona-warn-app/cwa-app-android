package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.storage.LocalData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.Before

class ScanResultTest {
    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    @MockK
    private lateinit var scanResult: ScanResult

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(scanResult)
        every { scanResult.isValid } returns false
    }

    @Test
    fun containsValidGUID() {
        //valid test
        scanResult = ScanResult("https://localhost/?$guid")
        scanResult.isValid shouldBe true

        // more invalid tests checks
        scanResult = ScanResult("http://localhost/?$guid")
        scanResult.isValid shouldBe false
        scanResult = ScanResult("https://localhost/?")
        scanResult.isValid shouldBe false
        scanResult = ScanResult("htps://wrongformat.com")
        scanResult.isValid shouldBe false
        scanResult = ScanResult("https://localhost/%20?3D6D08-3567F3F2-4DCF-43A3-8737-4CD1F87D6FDA")
        scanResult.isValid shouldBe false
        scanResult = ScanResult("https://some-host.com/?3D6D08-3567F3F2-4DCF-43A3-8737-4CD1F87D6FDA")
        scanResult.isValid shouldBe false
        scanResult = ScanResult("https://localhost/?3567F3F2-4DCF-43A3-8737-4CD1F87D6FDA")
        scanResult.isValid shouldBe false
        scanResult = ScanResult("https://localhost/?4CD1F87D6FDA")
        scanResult.isValid shouldBe false
    }

    @Test
    fun extractGUID() {
        MatcherAssert.assertThat(
            scanResult.extractGUID("https://localhost/?$guid"),
            CoreMatchers.equalTo(guid)
        )
    }
}

