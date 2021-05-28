package de.rki.coronawarnapp.coronatest.qrcode

import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestQRCodeTest : BaseTest() {

    private val instancePCR = CoronaTestQRCode.PCR("pcr")
    private val instanceRA = CoronaTestQRCode.RapidAntigen("ra", createdAt = Instant.EPOCH)

    @Test
    fun `PCR defaults`() {
        instancePCR.apply {
            isDccSupportedbyPoc shouldBe true
            isDccConsentGiven shouldBe false
            dateOfBirth shouldBe null
        }
    }

    @Test
    fun `RA defaults`() {
        instanceRA.apply {
            isDccSupportedbyPoc shouldBe false
            isDccConsentGiven shouldBe false
            dateOfBirth shouldBe null
        }
    }
}
